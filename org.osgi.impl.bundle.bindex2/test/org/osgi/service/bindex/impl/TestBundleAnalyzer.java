package org.osgi.service.bindex.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;

import org.osgi.framework.Version;
import org.osgi.service.bindex.Capability;
import org.osgi.service.bindex.Requirement;

public class TestBundleAnalyzer extends TestCase {
	
	public void testContentAndIdentity() throws Exception {
		BundleAnalyzer a = new BundleAnalyzer();
		LinkedList<Capability> caps = new LinkedList<Capability>();
		LinkedList<Requirement> reqs = new LinkedList<Requirement>();
		
		a.analyseResource(new JarResource(new File("testdata/org.example.a.jar")), caps, reqs);
		
		assertEquals(2, caps.size());
		
		Capability idcap = caps.get(0);
		assertEquals("osgi.identity", idcap.getNamespace());
		assertEquals("org.example.a", idcap.getAttributes().get("osgi.identity"));
		assertEquals("osgi.bundle", idcap.getAttributes().get("type"));
		assertEquals(new Version("0.0.0"), idcap.getAttributes().get("version"));
		
		Capability content = caps.get(1);
		assertEquals("osgi.content", content.getNamespace());
		assertEquals("testdata/org.example.a.jar", content.getAttributes().get("osgi.content"));
		assertEquals(1104L, content.getAttributes().get("size"));
	}
	
	public void testDescription() throws Exception {
		BundleAnalyzer a = new BundleAnalyzer();
		LinkedList<Capability> caps = new LinkedList<Capability>();
		LinkedList<Requirement> reqs = new LinkedList<Requirement>();
		
		a.analyseResource(new JarResource(new File("testdata/org.example.a.jar")), caps, reqs);
		
		
		String desc = (String) findCaps("osgi.content", caps).get(0).getAttributes().get("description");
		
		assertEquals("Example Bundle A", desc);
	}
	
	public void testDescriptionTranslated() throws Exception {
		BundleAnalyzer a = new BundleAnalyzer();
		LinkedList<Capability> caps = new LinkedList<Capability>();
		LinkedList<Requirement> reqs = new LinkedList<Requirement>();
		
		a.analyseResource(new JarResource(new File("testdata/org.example.b.jar")), caps, reqs);
		
		String desc = (String) findCaps("osgi.content", caps).get(0).getAttributes().get("description");
		assertEquals("Example Bundle B", desc);
	}
	
	public void testPackageExports() throws Exception {
		BundleAnalyzer a = new BundleAnalyzer();
		LinkedList<Capability> caps = new LinkedList<Capability>();
		LinkedList<Requirement> reqs = new LinkedList<Requirement>();
		
		a.analyseResource(new JarResource(new File("testdata/org.example.c.jar")), caps, reqs);
		
		Capability export = findCaps("osgi.wiring.package", caps).get(0);
		assertEquals("org.example.a", export.getAttributes().get("osgi.wiring.package"));
		assertEquals(new Version(1, 0, 0), export.getAttributes().get("version"));
	}
	
	public void testPackageExportUses() throws Exception {
		BundleAnalyzer a = new BundleAnalyzer();
		LinkedList<Capability> caps = new LinkedList<Capability>();
		LinkedList<Requirement> reqs = new LinkedList<Requirement>();
		
		a.analyseResource(new JarResource(new File("testdata/org.example.d.jar")), caps, reqs);
		
		List<Capability> exports = findCaps("osgi.wiring.package", caps);
		assertEquals(2, exports.size());

		assertEquals("org.example.b", exports.get(0).getAttributes().get("osgi.wiring.package"));
		assertEquals("org.example.a", exports.get(0).getDirectives().get("uses"));
		assertEquals("org.example.a", exports.get(1).getAttributes().get("osgi.wiring.package"));
	}
	
	public void testPackageImports() throws Exception {
		BundleAnalyzer a = new BundleAnalyzer();
		LinkedList<Capability> caps = new LinkedList<Capability>();
		LinkedList<Requirement> reqs = new LinkedList<Requirement>();
		
		a.analyseResource(new JarResource(new File("testdata/org.example.e.jar")), caps, reqs);
		
		Requirement pkgImport = findReqs("osgi.wiring.package", reqs).get(0);
		assertEquals("(&(osgi.wiring.package=org.example.a)(version>=1.0.0)(!(version>=2.0.0)))", pkgImport.getDirectives().get("filter"));
	}
	
	private static List<Capability> findCaps(String namespace, Collection<Capability> caps) {
		List<Capability> result = new ArrayList<Capability>();
		
		for (Capability cap : caps) {
			if (namespace.equals(cap.getNamespace()))
				result.add(cap);
		}
		
		return result;
	}
	
	private static List<Requirement> findReqs(String namespace, Collection<Requirement> reqs) {
		List<Requirement> result = new ArrayList<Requirement>();
		
		for (Requirement req : reqs) {
			if (namespace.equals(req.getNamespace()))
				result.add(req);
		}
		
		return result;
	}
}