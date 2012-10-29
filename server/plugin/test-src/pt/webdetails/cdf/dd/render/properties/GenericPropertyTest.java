package pt.webdetails.cdf.dd.render.properties;

import junit.framework.Assert;

import org.junit.Test;

public class GenericPropertyTest {
	
	//Literal tests
	
	@Test
	public void testLiteral_0_GetFunctionParameter() {
		GenericProperty gp = new GenericProperty();
		String input = "potatos";
		input.replace("\"", "\\\"").replaceAll("(\\$\\{[^}]*\\})", "\"+ $1 + \"");
		String result = gp.getFunctionParameter(input, true);
		System.out.println(result);
		System.out.println("function(){" + (true ? " return \"" + input + "\"" : input) + ";}");		
		Assert.assertEquals("function(){" + (true ? " return \"" + input + "\"" : input) + ";}", result);
	}
	
	@Test
	public void testLiteral_1_GetFunctionParameter() {
		GenericProperty gp = new GenericProperty();
		String input = "functio(){return 'literal';} ";
		input.replace("\"", "\\\"").replaceAll("(\\$\\{[^}]*\\})", "\"+ $1 + \""); 
		String result = gp.getFunctionParameter(input, true);
		System.out.println(result);
		System.out.println("function(){" + (true ? " return \"" + input + "\"" : input) + ";}");		
		Assert.assertEquals("function(){" + (true ? " return \"" + input + "\"" : input) + ";}", result);
	}
	
	@Test
	public void testLiteral_2_GetFunctionParameter() {
		GenericProperty gp = new GenericProperty();
		String input = "function f d() { return 'literal';} ";
		input.replace("\"", "\\\"").replaceAll("(\\$\\{[^}]*\\})", "\"+ $1 + \"");
		String result = gp.getFunctionParameter(input, true);
		System.out.println(result);
		System.out.println("function(){" + (true ? " return \"" + input + "\"" : input) + ";}");		
		Assert.assertEquals("function(){" + (true ? " return \"" + input + "\"" : input) + ";}", result);
	}

	// Functions testing
	
	@Test
	public void testFunction_0_GetFunctionParameter() {
		GenericProperty gp = new GenericProperty();
		String input = "function (){retun \"literal\";} "; 
		String result = gp.getFunctionParameter(input, true);
		Assert.assertEquals(input, result);
	}

	@Test
	public void testFunction_1_GetFunctionParameter() {
		GenericProperty gp = new GenericProperty();
		String input = "function (){retun \"literal\";} "; 
		String result = gp.getFunctionParameter(input, true);
		Assert.assertEquals(input, result);
	}

	@Test
	public void testFunction_2_GetFunctionParameter() {
		GenericProperty gp = new GenericProperty();
		String input = "function () {retun \"literal\";} "; 
		String result = gp.getFunctionParameter(input, true);
		Assert.assertEquals(input, result);
	}

	@Test
	public void testFunction_3_GetFunctionParameter() {
		GenericProperty gp = new GenericProperty();
		String input = "function f() { \rreturn \"literal\";\r}\r\r\r "; 
		String result = gp.getFunctionParameter(input, true);
		Assert.assertEquals(input, result);
	}
	
	@Test
	public void testFunction_4_GetFunctionParameter() {
		GenericProperty gp = new GenericProperty();
		String input = "function \n f() { return \"literal\";}"; 
		String result = gp.getFunctionParameter(input, true);
		Assert.assertEquals(input, result);
	}
	
	@Test
	public void testFunction_5_GetFunctionParameter() {
		GenericProperty gp = new GenericProperty();
		String input = " function \n()\n {\n int x;\n return \"literal\";\n}\n\n\n "; 
		String result = gp.getFunctionParameter(input, true);
		Assert.assertEquals(input, result);
	}
	
	@Test
	public void testFunction_6_GetFunctionParameter() {
		GenericProperty gp = new GenericProperty();
		String input = "function \n() {\n \n return \"literal\";} \n "; 
		String result = gp.getFunctionParameter(input, true);
		Assert.assertEquals(input, result);
	}
	
	@Test
	public void testFunction_7_GetFunctionParameter() {
		GenericProperty gp = new GenericProperty();
		String input = "\n function f() \t { return \"blabfffffffffddddla\";\t\t\t\t }"; 
		String result = gp.getFunctionParameter(input, true);
		Assert.assertEquals(input, result);
	}
	
	@Test
	public void testFunction_8_GetFunctionParameter() {
		GenericProperty gp = new GenericProperty();
		String input = "\n function f() { return \"blabfffffffffddddla\"; }"; 
		String result = gp.getFunctionParameter(input, true);
		Assert.assertEquals(input, result);
	}
	
	@Test
	public void testFunction_9_GetFunctionParameter() {
		GenericProperty gp = new GenericProperty();
		String input = "\n function(x, y){ \n    return \"blabfffffffffddddla\"; \n}"; 
		String result = gp.getFunctionParameter(input, true);
		Assert.assertEquals(input, result);
	}
	
	@Test
	public void testFunction_10_GetFunctionParameter() {
		GenericProperty gp = new GenericProperty();
		String input = " \nfunction\nf()\n{\n\nreturn \n\"literal\";\n}\n\n\n "; 
		String result = gp.getFunctionParameter(input, true);
		Assert.assertEquals(input, result);
	}
	
	@Test
	public void testFunction_11_GetFunctionParameter() {
		GenericProperty gp = new GenericProperty();
		String input = " \n\t\tfunction\nf()\n{\n\nreturn \n\t\t\"literal\";\n}\n\n\n "; 
		String result = gp.getFunctionParameter(input, true);
		Assert.assertEquals(input, result);
	}

}
