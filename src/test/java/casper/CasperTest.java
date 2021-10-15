package casper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.reflect.MethodUtils;

import sacha.reflect.classloading.PermissiveClassLoader;
import spoon.Launcher;
import bcornu.nullmode.DebugInfo;
import bcornu.nullmode.DeluxeNPE;
import bcornu.nullmode.LocatedNPE;
import bcornu.nullmode.NullGhost;

public class CasperTest {

	@org.junit.Test
	public void testPermissiveCLassLoader() throws Throwable {
		PermissiveClassLoader pcl = new PermissiveClassLoader(getClass().getClassLoader());
		Class c;


		// final in source code and i the default class loader
		assertTrue(Modifier.isFinal(Foo.class.getModifiers()));

		// not final with our specific class loader
		c = pcl.loadClass("casper.Foo");
		assertFalse(Modifier.isFinal(c.getModifiers()));

		c = pcl.loadNullClass(Foo.class);
		assertEquals("casper.FooNullified", c.getName());
		assertEquals("bcornu.nullmode.NullGhost", c.getInterfaces()[0].getName());
		assertTrue(c.newInstance() instanceof NullGhost);

		c = pcl.loadNullClass(Object.class);
		assertEquals("jav.lang.ObjectNullified", c.getName());
		assertTrue(c.newInstance() instanceof NullGhost);

	}

	public void compileSpooned() throws IOException {
	    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
	    DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
	    StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);
	    Iterable<? extends JavaFileObject> compilationUnits = fileManager
	        .getJavaFileObjects(new File("./spooned").listFiles());
	    JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnostics, null,
	        null, compilationUnits);
	    boolean success = task.call();
	    if (success == false) {
			System.out.println(diagnostics.getDiagnostics());
		}

	    fileManager.close();
	    System.out.println("compilation success: " + success);
	  }

	@org.junit.Test
	public void testCasper1() throws Throwable {
		mainTest(GhostClassType.BINARY_GHOST);
	}
	@org.junit.Test
	public void testCasper2() throws Throwable {
		mainTest(GhostClassType.SOURCE_GHOST);
	}

	enum GhostClassType {
		SOURCE_GHOST,
		BINARY_GHOST
	}


	/**
	 * @param type
	 * @throws Throwable
	 */
	public void mainTest(GhostClassType type) throws Throwable {

		Runtime.getRuntime().exec("./clean-spooned.sh");

		String clazz = "FooCasper";

		Launcher l = new Launcher();
		l.getModelBuilder().setBinaryOutputDirectory(new File("spooned"));
		l.setArgs(new String[] {
				"--source-classpath", "target/classes",
				"-i", "src/test/resources/"+clazz+".java",
				"--compile", "--with-imports", "--lines", "--disable-comments"
				}
);
		// this processor must come first
		if (type == GhostClassType.SOURCE_GHOST) {
		l.addProcessor("bcu.transformer.processors.GhostClassCreator");
		}

		l.addProcessor("bcu.transformer.processors.ParamProcessor");// unghostify

		l.addProcessor("bcu.transformer.processors.AssignmentModifier"); // inception assignment
		// symptom field access and array accesses
		l.addProcessor("bcu.transformer.processors.TargetModifier");

		// inception
		l.addProcessor("bcu.transformer.processors.NullLiteralProcessor"); // inception literal
		l.addProcessor("bcu.transformer.processors.VariableModifier"); // inception local variable
		l.addProcessor("bcu.transformer.processors.FieldModifier"); // inception default value of fields
		l.addProcessor("bcu.transformer.processors.ArgumentsModifier"); // inception arguments
		l.addProcessor("bcu.transformer.processors.ReturnModifier");

		l.addProcessor("bcu.transformer.processors.ComparizonModifier");
		l.addProcessor(bcu.transformer.processors.TargetAdder.class.getCanonicalName());
		l.run();

		// compiliing (need for --compile in Spoon, but a bug there)
		//Runtime.getRuntime().exec("./compile-spooned.sh");
		compileSpooned();

		Thread.sleep(800);
		assertTrue(new File("./spooned/FooCasper.class").exists());

		if (type == GhostClassType.SOURCE_GHOST) {
			assertTrue(new File("./spooned/FooCasperNullified.class").exists());
		}

		Class<?> helloClass;

		ClassLoader cl = null;
		// solution with binary ghost and PermissiveClassLoader
		if (type == GhostClassType.BINARY_GHOST) {
			cl = new PermissiveClassLoader(getClass().getClassLoader());
			((PermissiveClassLoader )cl).addURL("./spooned/");
		} else if (type == GhostClassType.SOURCE_GHOST) {
		  // solution with source ghost and classical URL classical class loader
		  cl =  new URLClassLoader(new URL[] { new File("./spooned/").toURI().toURL()}/* parent */ , this.getClass().getClassLoader()		);
		}

		helloClass = cl.loadClass(clazz);

		Object o = helloClass.newInstance();
		Object ghostInstance;

		// a ghost is created with hard-coded "return null"
		ghostInstance = MethodUtils.invokeExactMethod(o, "foo2");
		assertTrue(ghostInstance instanceof NullGhost);

		{
		// a ghost is created with non initialized fields
		ghostInstance = MethodUtils.invokeExactMethod(o, "foo3");
		assertTrue(ghostInstance instanceof NullGhost);
		List<String> events = ((DebugInfo)FieldUtils.readField(ghostInstance, "DNPE_data")).events;
		assertTrue(events.contains("inception: null initialized field f (FooCasper.java:8)"));

		//System.out.println(Arrays.toString(helloClass.getDeclaredMethods()));
		}

		try {
			MethodUtils.invokeExactMethod(o, "bug1");
			fail(); // no npe
		} catch (InvocationTargetException e) {

			Throwable npe = e.getCause();

			assertTrue(npe instanceof NullPointerException);
			assertTrue(npe instanceof DeluxeNPE);

			System.out.println(npe.toString());

			List<String> events = ((DebugInfo)FieldUtils.readField(npe, "data")).events;
			//"returned null in method foo2 (FooCasper.java:28)"+"\n"+
			//"returned null in method foo (FooCasper.java:23)"+"\n"+
			assertTrue(events.contains("inception: null initialized null (FooCasper.java:28)"));
			assertTrue(events.contains("assigned null to g (FooCasper.java:14)"));
			assertTrue(events.contains("assigned null to f (FooCasper.java:15)"));
			assertTrue(events.contains("throws NPE at FooCasper.bug1(FooCasper.java:18)"));
		}


		try {
			MethodUtils.invokeExactMethod(o, "bug2");
			fail(); // no npe
		} catch (InvocationTargetException e) {

			Throwable npe = e.getCause();

			//System.out.println(npe.toString());
			assertTrue(npe instanceof NullPointerException);
			assertTrue(npe instanceof DeluxeNPE);

			List<String> events = ((DebugInfo)FieldUtils.readField(npe, "data")).events;
			assertTrue(events.contains("inception: null initialized null (FooCasper.java:43)"));
			assertTrue(events.contains("parameter o is null in foo5 at (FooCasper.java:38)"));
			assertTrue(events.contains("returned null in method foo5 (FooCasper.java:39)"));
			assertTrue(events.contains("field access on null at (FooCasper.java:43)"));
			assertTrue(events.contains("throws NPE at FooCasper.bug2(FooCasper.java:43)"));
		}

		try {
			MethodUtils.invokeExactMethod(o, "bug3");
			fail(); // no npe
		} catch (InvocationTargetException e) {
			Throwable npe = e.getCause();
			assertTrue(npe instanceof NullPointerException);
			assertTrue(npe instanceof LocatedNPE);
		}

		if (type == GhostClassType.SOURCE_GHOST) {
			// TODO implement for binary instrumentation also
			// toSring
			try {
				MethodUtils.invokeExactMethod(o, "toString_support");
				fail("no npe thrown");
			} catch (InvocationTargetException e) {
				Throwable npe = e.getCause();
				assertTrue(npe instanceof NullPointerException);
				assertTrue(npe instanceof DeluxeNPE);
			}
		}

		// arrays
		try {
			MethodUtils.invokeExactMethod(o, "array_support");
			fail("no npe thrown"); // no npe
		} catch (InvocationTargetException e) {
			Throwable npe = e.getCause();
			assertTrue(npe instanceof NullPointerException);
			assertTrue(npe instanceof DeluxeNPE);
			List<String> events = ((DebugInfo)FieldUtils.readField(npe, "data")).events;
			assertTrue(events.contains("assigned null to array[1] (FooCasper.java:75)"));
		}

		try {
			MethodUtils.invokeExactMethod(o, "literal");
			fail("no npe thrown"); // no npe
		} catch (InvocationTargetException e) {
			Throwable npe = e.getCause();
			assertTrue(npe instanceof NullPointerException);
			assertTrue(npe instanceof DeluxeNPE);
			List<String> events = ((DebugInfo)FieldUtils.readField(npe, "data")).events;
			assertTrue(events.contains("inception: null initialized null (FooCasper.java:81)"));
			assertTrue(events.contains("throws NPE at FooCasper.literal(FooCasper.java:82)"));
		}

		try {
			MethodUtils.invokeExactMethod(o, "literal2");
			fail("no npe thrown"); // no npe
		} catch (InvocationTargetException e) {
			Throwable npe = e.getCause();
			assertTrue(npe instanceof NullPointerException);
			assertTrue(npe instanceof DeluxeNPE);
			System.out.println(npe);
			List<String> events = ((DebugInfo)FieldUtils.readField(npe, "data")).events;
			assertTrue(events.contains("inception: assigned null to tab (FooCasper.java:87)"));
			assertTrue(events.contains("throws NPE at FooCasper.literal2(FooCasper.java:88)"));
		}
	}
}
