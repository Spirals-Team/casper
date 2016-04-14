package sacha.reflect.classloading;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import jav.lang.ObjectNullified;
import jav.util.SetNullified;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import sacha.asm.visitor.FinalRemover;
import sacha.asm.visitor.NullClassCreator;

@SuppressWarnings("rawtypes")
public class PermissiveClassLoader extends ClassLoader{

	/**
	 * same process than URLClassLoader
	 */
	List<String> urls = new ArrayList<>();
	
	/**
	 * accessible classes
	 */
	Map<String,Class<?>> classes = new HashMap<>();
	
	/**
	 * classes not accessible yet
	 */
	Map<String,Class<?>> hiddenClasses = new HashMap<>();

	public PermissiveClassLoader(ClassLoader parent){
		super(parent);
		String classPath = System.getProperty("java.class.path");
		for (String classpathElement : splitClassPath(classPath)) {
			addURL(classpathElement);
		}
		classes.put("sacha.reflect.classloading.PermissiveClassLoader", this.getClass());
		
		String o;
		
		hiddenClasses.put("java.lang.Object", ObjectNullified.class);		
		hiddenClasses.put("java.collections.Set", SetNullified.class);		
	}
	
	/**
	 * same process than URLClassLoader
	 */
	public void addURL(String url) {
		urls.add(url);
	}

	private static List<String> splitClassPath(String classPath) {
		final String separator = System.getProperty("path.separator");
		return Arrays.asList(classPath.split(separator));
	}
	
//	/**
//	 * DO NOT USE
//	 */
//	@Override
//	protected Class<?> findClass(final String name)
//			throws ClassNotFoundException {
//		throw new ClassNotFoundException("do not use this method on this classloader");
//	}
//	
	
	public Class getModifiedClass(String qualifiedName) {
		return classes.get(qualifiedName);
	}
	/**
	 * load the class with the given name and removes all final
	 * remove the final keywords on class and methods
	 */
	@Override
	public Class<?> loadClass(String classQualifiedName) throws ClassNotFoundException {
		
		// already transformed
		if(classes.containsKey(classQualifiedName)){
			return classes.get(classQualifiedName);
		}
		
		// we don't ghostify the framework itself
		if (classQualifiedName.startsWith("bcornu")) {
			return super.loadClass(classQualifiedName);
		}
		
        try {
			for (String url : urls) {
				File file = new File(url);
				if(!file.exists()){
					System.err.println("removed inexistant file :"+file.getAbsolutePath());
					urls.remove(url);
					continue;
				}else if(file.getName().endsWith(".jar")){
					String tmpName = classQualifiedName.replaceAll("\\.", "/")+".class";
					URL tmpUrl = new URL("jar:file:"+file.getAbsolutePath()+"!/"+tmpName);
					try{
						InputStream is = tmpUrl.openStream();
						ClassReader classReader=new ClassReader(is);
						ClassWriter cw=new ClassWriter(ClassWriter.COMPUTE_MAXS);
						
						FinalRemover fr = new FinalRemover(cw);
						classReader.accept(fr, 0);
						
						Class<?> res = defineClass(classQualifiedName, cw.toByteArray());
						classes.put(classQualifiedName, res);
//						System.out.println("own jar class loaded :"+name);
						return res;
					}catch(FileNotFoundException e){
						continue;
					}
				}else if(file.isDirectory()){
					String tmpName = classQualifiedName.replaceAll("\\.", "/")+".class";
					File classFile = new File(file,tmpName);
					if( ! classFile.exists()){
						continue;
					}
					InputStream is = new FileInputStream(classFile);
					ClassReader classReader=new ClassReader(is);
					ClassWriter cw=new ClassWriter(ClassWriter.COMPUTE_MAXS);
					
					FinalRemover fr = new FinalRemover(cw);
					classReader.accept(fr, 0);
					
					System.err.println("class loading with final remover :"+classQualifiedName);
					Class<?> res = defineClass(classQualifiedName, cw.toByteArray());
					classes.put(classQualifiedName, res);
					return res;
				}else{
					System.err.println("what is that? :"+file.getAbsolutePath());
					continue;
				}
			} // end for
			
			
				return super.loadClass(classQualifiedName);

//			System.err.println("not found :"+name);
//			throw new ClassNotFoundException(name);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private Class<?> defineClass(String name, byte[] b) {
		return defineClass(name, b, 0, b.length);
	}

	/**
	 * return the corresponding nullInstanciable class
	 * if possible
	 */
	public Class loadNullClass(Class clazz) {
		Class c;
		if(hiddenClasses.containsKey(clazz.getCanonicalName())){//cannot modify the original bytecode
			if(Modifier.isFinal(clazz.getModifiers())){
				return null;
			}else{
				c = loadExistingNullClass(clazz.getCanonicalName());
			}
		}else{
			c = createNullGhostClass(clazz.getCanonicalName(),clazz.isInterface());
		}
		if (!c.getName().endsWith("Nullified")) {
			throw new RuntimeException("postcondition violated");
		}
		return c;
	}

	/**
	 * try to load the corresponding nullinstaciable class from 
	 * "hard-written" source code
	 */
	private Class loadExistingNullClass(String name) {
		String newName = name+"Nullified";
		if(newName.startsWith("java.")){
			newName = newName.replaceFirst("java", "jav");
		}
		if(classes.containsKey(newName)){
			return classes.get(newName);
		}
		try{
			return loadClass(newName);
		}catch(ClassNotFoundException cnfe){
			if(cnfe.getMessage().equals("do not use this method on this classloader"))
				System.err.println("class not nullified : "+name);
			else
				cnfe.printStackTrace();
		}catch(Throwable t){
			t.printStackTrace();
		}
		return null;
	}

	/**
	 * create the corresponding nullinstanciable class
	 * by modifying the original class
	 * @param b 
	 */
	private Class createNullGhostClass(String name, boolean b) {
		System.err.println("created null ghost class for "+name);
		if (name.endsWith("Nullified")) {
			throw new IllegalArgumentException("can not create a ghost of a ghost");
		}

		String newName = name+"Nullified";
		if(classes.containsKey(newName)){
			return classes.get(newName);
		}
        try {
			for (String url : urls) {
				File file = new File(url);
				if(!file.exists()){
					System.err.println("removed inexistant file :"+file.getAbsolutePath());
					urls.remove(url);
					continue;
				}else if(file.getName().endsWith(".jar")){
					String tmpName = name.replace('.', '/')+".class";
					URL tmpUrl = new URL("jar:file:"+file.getAbsolutePath()+"!/"+tmpName);
					try{
						InputStream is = tmpUrl.openStream();
						ClassReader classReader=new ClassReader(is);
						ClassWriter cw=new ClassWriter(ClassWriter.COMPUTE_MAXS);
						
						NullClassCreator modifier = new NullClassCreator(cw,name.replace('.', '/'),
								newName.replace('.', '/'),b);
				        classReader.accept(modifier, ClassReader.EXPAND_FRAMES);
						
				        File outDir=new File("out/sacha/generated");
				        outDir.mkdirs();
				        DataOutputStream dout=new DataOutputStream(new FileOutputStream(new File(outDir,newName+".class")));
				        dout.write(cw.toByteArray());
				        dout.flush();
				        dout.close();
				        
						Class<?> res = defineClass(newName, cw.toByteArray());
						classes.put(newName, res);
						return res;
					}catch(FileNotFoundException e){
						continue;
					}
				}else if(file.isDirectory()){
					String tmpName = name.replaceAll("\\.", "/")+".class";
					File classFile = new File(file,tmpName);
					if( ! classFile.exists()){
						continue;
					}
					InputStream is = new FileInputStream(classFile);
					ClassReader classReader=new ClassReader(is);
					ClassWriter cw=new ClassWriter(ClassWriter.COMPUTE_MAXS);
					
					NullClassCreator modifier = new NullClassCreator(cw,name.replace('.', '/'),
							newName.replace('.', '/'),b);
			        classReader.accept(modifier, ClassReader.EXPAND_FRAMES);
					
			        File outDir=new File("out/sacha/generated");
			        outDir.mkdirs();
			        DataOutputStream dout=new DataOutputStream(new FileOutputStream(new File(outDir,newName+".class")));
			        dout.write(cw.toByteArray());
			        dout.flush();
			        dout.close();
			        
					Class<?> res = defineClass(newName, cw.toByteArray());
					classes.put(newName, res);
					return res;
				}else{
					System.err.println("what is that? :"+file.getAbsolutePath());
					continue;
				}
			}
			return null;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	
}
