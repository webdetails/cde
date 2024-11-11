/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/
{
"denormalizedMdx_mondrianJndi": {
	"metadata": {
		"name": "denormalizedMdx over mondrianJndi",
		"conntype": "mondrian.jndi",
		"datype": "denormalizedMdx",
		"group": "MDX",
		"groupdesc": "MDX Queries"
	},
	"definition": {
		"connection": {
			"id": {"type": "STRING", "placement": "ATTRIB"},
			"catalog": {"type": "STRING", "placement": "CHILD"},
			"jndi": {"type": "STRING", "placement": "CHILD"}
		},
		"dataaccess": {
			"id": {"type": "STRING", "placement": "ATTRIB"},
			"access": {"type": "STRING", "placement": "ATTRIB"},
			"parameters": {"type": "ARRAY", "placement": "CHILD"},
			"output": {"type": "ARRAY", "placement": "CHILD"},
			"columns": {"type": "ARRAY", "placement": "CHILD"},
			"query": {"type": "STRING", "placement": "CHILD"},
			"connection": {"type": "STRING", "placement": "ATTRIB"},
			"cache": {"type": "BOOLEAN", "placement": "ATTRIB"},
			"cacheDuration": {"type": "NUMERIC", "placement": "ATTRIB"}
		}
	}
},
"denormalizedMdx_mondrianJdbc": {
	"metadata": {
		"name": "denormalizedMdx over mondrianJdbc",
		"conntype": "mondrian.jdbc",
		"datype": "denormalizedMdx",
		"group": "MDX",
		"groupdesc": "MDX Queries"
	},
	"definition": {
		"connection": {
			"id": {"type": "STRING", "placement": "ATTRIB"},
			"catalog": {"type": "STRING", "placement": "CHILD"},
			"driver": {"type": "STRING", "placement": "CHILD"},
			"url": {"type": "STRING", "placement": "CHILD"},
			"user": {"type": "STRING", "placement": "CHILD"},
			"pass": {"type": "STRING", "placement": "CHILD"}
		},
		"dataaccess": {
			"id": {"type": "STRING", "placement": "ATTRIB"},
			"access": {"type": "STRING", "placement": "ATTRIB"},
			"parameters": {"type": "ARRAY", "placement": "CHILD"},
			"output": {"type": "ARRAY", "placement": "CHILD"},
			"columns": {"type": "ARRAY", "placement": "CHILD"},
			"query": {"type": "STRING", "placement": "CHILD"},
			"connection": {"type": "STRING", "placement": "ATTRIB"},
			"cache": {"type": "BOOLEAN", "placement": "ATTRIB"},
			"cacheDuration": {"type": "NUMERIC", "placement": "ATTRIB"}
		}
	}
},
"denormalizedOlap4j_olap4j": {
	"metadata": {
		"name": "denormalizedOlap4j over olap4j",
		"conntype": "olap4j.defaultolap4j",
		"datype": "denormalizedOlap4j",
		"group": "OLAP4J",
		"groupdesc": "OLAP4J Queries"
	},
	"definition": {
		"connection": {
			"id": {"type": "STRING", "placement": "ATTRIB"},
			"driver": {"type": "STRING", "placement": "CHILD"},
			"url": {"type": "STRING", "placement": "CHILD"},
			"role": {"type": "STRING", "placement": "CHILD"},
			"property": {"type": "STRING", "placement": "CHILD"}
		},
		"dataaccess": {
			"id": {"type": "STRING", "placement": "ATTRIB"},
			"access": {"type": "STRING", "placement": "ATTRIB"},
			"parameters": {"type": "ARRAY", "placement": "CHILD"},
			"output": {"type": "ARRAY", "placement": "CHILD"},
			"columns": {"type": "ARRAY", "placement": "CHILD"},
			"query": {"type": "STRING", "placement": "CHILD"},
			"connection": {"type": "STRING", "placement": "ATTRIB"},
			"cache": {"type": "BOOLEAN", "placement": "ATTRIB"},
			"cacheDuration": {"type": "NUMERIC", "placement": "ATTRIB"}
		}
	}
},
"join": {
	"metadata": {
		"name": "join",
		"datype": "join",
		"group": "NONE",
		"groupdesc": "Compound Queries"
	},
	"definition": {
		"dataaccess": {
			"id": {"type": "STRING", "placement": "ATTRIB"},
			"left": {"type": "STRING", "placement": "CHILD"},
			"right": {"type": "STRING", "placement": "CHILD"},
			"parameters": {"type": "STRING", "placement": "CHILD"},
			"output": {"type": "ARRAY", "placement": "CHILD"},
			"joinType": {"type": "STRING", "placement": "CHILD"}
		}
	}
},
"kettle_kettleTransFromFile": {
	"metadata": {
		"name": "kettle over kettleTransFromFile",
		"conntype": "kettle.TransFromFile",
		"datype": "kettle",
		"group": "KETTLE",
		"groupdesc": "KETTLE Queries"
	},
	"definition": {
		"connection": {
			"ktrFile": {"type": "STRING", "placement": "CHILD"},
			"variables": {"type": "ARRAY", "placement": "CHILD"}
		},
		"dataaccess": {
			"id": {"type": "STRING", "placement": "ATTRIB"},
			"access": {"type": "STRING", "placement": "ATTRIB"},
			"parameters": {"type": "ARRAY", "placement": "CHILD"},
			"output": {"type": "ARRAY", "placement": "CHILD"},
			"columns": {"type": "ARRAY", "placement": "CHILD"},
			"query": {"type": "STRING", "placement": "CHILD"},
			"connection": {"type": "STRING", "placement": "ATTRIB"},
			"cache": {"type": "BOOLEAN", "placement": "ATTRIB"},
			"cacheDuration": {"type": "NUMERIC", "placement": "ATTRIB"}
		}
	}
},
"mdx_mondrianJndi": {
	"metadata": {
		"name": "mdx over mondrianJndi",
		"conntype": "mondrian.jndi",
		"datype": "mdx",
		"group": "MDX",
		"groupdesc": "MDX Queries"
	},
	"definition": {
		"connection": {
			"id": {"type": "STRING", "placement": "ATTRIB"},
			"catalog": {"type": "STRING", "placement": "CHILD"},
			"jndi": {"type": "STRING", "placement": "CHILD"}
		},
		"dataaccess": {
			"id": {"type": "STRING", "placement": "ATTRIB"},
			"access": {"type": "STRING", "placement": "ATTRIB"},
			"parameters": {"type": "ARRAY", "placement": "CHILD"},
			"output": {"type": "ARRAY", "placement": "CHILD"},
			"columns": {"type": "ARRAY", "placement": "CHILD"},
			"query": {"type": "STRING", "placement": "CHILD"},
			"connection": {"type": "STRING", "placement": "ATTRIB"},
			"cache": {"type": "BOOLEAN", "placement": "ATTRIB"},
			"cacheDuration": {"type": "NUMERIC", "placement": "ATTRIB"},
			"bandedMode": {"type": "STRING", "placement": "CHILD"}
		}
	}
},
"mdx_mondrianJdbc": {
	"metadata": {
		"name": "mdx over mondrianJdbc",
		"conntype": "mondrian.jdbc",
		"datype": "mdx",
		"group": "MDX",
		"groupdesc": "MDX Queries"
	},
	"definition": {
		"connection": {
			"id": {"type": "STRING", "placement": "ATTRIB"},
			"catalog": {"type": "STRING", "placement": "CHILD"},
			"driver": {"type": "STRING", "placement": "CHILD"},
			"url": {"type": "STRING", "placement": "CHILD"},
			"user": {"type": "STRING", "placement": "CHILD"},
			"pass": {"type": "STRING", "placement": "CHILD"}
		},
		"dataaccess": {
			"id": {"type": "STRING", "placement": "ATTRIB"},
			"access": {"type": "STRING", "placement": "ATTRIB"},
			"parameters": {"type": "ARRAY", "placement": "CHILD"},
			"output": {"type": "ARRAY", "placement": "CHILD"},
			"columns": {"type": "ARRAY", "placement": "CHILD"},
			"query": {"type": "STRING", "placement": "CHILD"},
			"connection": {"type": "STRING", "placement": "ATTRIB"},
			"cache": {"type": "BOOLEAN", "placement": "ATTRIB"},
			"cacheDuration": {"type": "NUMERIC", "placement": "ATTRIB"},
			"bandedMode": {"type": "STRING", "placement": "CHILD"}
		}
	}
},
"mql_metadata": {
	"metadata": {
		"name": "mql over metadata",
		"conntype": "metadata.metadata",
		"datype": "mql",
		"group": "MQL",
		"groupdesc": "MQL Queries"
	},
	"definition": {
		"connection": {
			"id": {"type": "STRING", "placement": "ATTRIB"},
			"xmiFile": {"type": "STRING", "placement": "CHILD"},
			"domainId": {"type": "STRING", "placement": "CHILD"}
		},
		"dataaccess": {
			"id": {"type": "STRING", "placement": "ATTRIB"},
			"access": {"type": "STRING", "placement": "ATTRIB"},
			"parameters": {"type": "ARRAY", "placement": "CHILD"},
			"output": {"type": "ARRAY", "placement": "CHILD"},
			"columns": {"type": "ARRAY", "placement": "CHILD"},
			"query": {"type": "STRING", "placement": "CHILD"},
			"connection": {"type": "STRING", "placement": "ATTRIB"},
			"cache": {"type": "BOOLEAN", "placement": "ATTRIB"},
			"cacheDuration": {"type": "NUMERIC", "placement": "ATTRIB"}
		}
	}
},
"olap4j_olap4j": {
	"metadata": {
		"name": "olap4j over olap4j",
		"conntype": "olap4j.defaultolap4j",
		"datype": "olap4j",
		"group": "OLAP4J",
		"groupdesc": "OLAP4J Queries"
	},
	"definition": {
		"connection": {
			"id": {"type": "STRING", "placement": "ATTRIB"},
			"driver": {"type": "STRING", "placement": "CHILD"},
			"url": {"type": "STRING", "placement": "CHILD"},
			"role": {"type": "STRING", "placement": "CHILD"},
			"property": {"type": "STRING", "placement": "CHILD"}
		},
		"dataaccess": {
			"id": {"type": "STRING", "placement": "ATTRIB"},
			"access": {"type": "STRING", "placement": "ATTRIB"},
			"parameters": {"type": "ARRAY", "placement": "CHILD"},
			"output": {"type": "ARRAY", "placement": "CHILD"},
			"columns": {"type": "ARRAY", "placement": "CHILD"},
			"query": {"type": "STRING", "placement": "CHILD"},
			"connection": {"type": "STRING", "placement": "ATTRIB"},
			"cache": {"type": "BOOLEAN", "placement": "ATTRIB"},
			"cacheDuration": {"type": "NUMERIC", "placement": "ATTRIB"}
		}
	}
},
"scriptable_scripting": {
	"metadata": {
		"name": "scriptable over scripting",
		"conntype": "scripting.scripting",
		"datype": "scriptable",
		"group": "SCRIPTING",
		"groupdesc": "SCRIPTING Queries"
	},
	"definition": {
		"connection": {
			"id": {"type": "STRING", "placement": "ATTRIB"},
			"language": {"type": "STRING", "placement": "CHILD"},
			"initscript": {"type": "STRING", "placement": "CHILD"}
		},
		"dataaccess": {
			"id": {"type": "STRING", "placement": "ATTRIB"},
			"access": {"type": "STRING", "placement": "ATTRIB"},
			"parameters": {"type": "ARRAY", "placement": "CHILD"},
			"output": {"type": "ARRAY", "placement": "CHILD"},
			"columns": {"type": "ARRAY", "placement": "CHILD"},
			"query": {"type": "STRING", "placement": "CHILD"},
			"connection": {"type": "STRING", "placement": "ATTRIB"},
			"cache": {"type": "BOOLEAN", "placement": "ATTRIB"},
			"cacheDuration": {"type": "NUMERIC", "placement": "ATTRIB"}
		}
	}
},
"sql_sqlJndi": {
	"metadata": {
		"name": "sql over sqlJndi",
		"conntype": "sql.jndi",
		"datype": "sql",
		"group": "SQL",
		"groupdesc": "SQL Queries"
	},
	"definition": {
		"connection": {
			"id": {"type": "STRING", "placement": "ATTRIB"},
			"jndi": {"type": "STRING", "placement": "CHILD"}
		},
		"dataaccess": {
			"id": {"type": "STRING", "placement": "ATTRIB"},
			"access": {"type": "STRING", "placement": "ATTRIB"},
			"parameters": {"type": "ARRAY", "placement": "CHILD"},
			"output": {"type": "ARRAY", "placement": "CHILD"},
			"columns": {"type": "ARRAY", "placement": "CHILD"},
			"query": {"type": "STRING", "placement": "CHILD"},
			"connection": {"type": "STRING", "placement": "ATTRIB"},
			"cache": {"type": "BOOLEAN", "placement": "ATTRIB"},
			"cacheDuration": {"type": "NUMERIC", "placement": "ATTRIB"}
		}
	}
},
"sql_sqlJdbc": {
	"metadata": {
		"name": "sql over sqlJdbc",
		"conntype": "sql.jdbc",
		"datype": "sql",
		"group": "SQL",
		"groupdesc": "SQL Queries"
	},
	"definition": {
		"connection": {
			"id": {"type": "STRING", "placement": "ATTRIB"},
			"driver": {"type": "STRING", "placement": "CHILD"},
			"url": {"type": "STRING", "placement": "CHILD"},
			"user": {"type": "STRING", "placement": "CHILD"},
			"pass": {"type": "STRING", "placement": "CHILD"}
		},
		"dataaccess": {
			"id": {"type": "STRING", "placement": "ATTRIB"},
			"access": {"type": "STRING", "placement": "ATTRIB"},
			"parameters": {"type": "ARRAY", "placement": "CHILD"},
			"output": {"type": "ARRAY", "placement": "CHILD"},
			"columns": {"type": "ARRAY", "placement": "CHILD"},
			"query": {"type": "STRING", "placement": "CHILD"},
			"connection": {"type": "STRING", "placement": "ATTRIB"},
			"cache": {"type": "BOOLEAN", "placement": "ATTRIB"},
			"cacheDuration": {"type": "NUMERIC", "placement": "ATTRIB"}
		}
	}
},
"union": {
	"metadata": {
		"name": "union",
		"datype": "union",
		"group": "NONE",
		"groupdesc": "Compound Queries"
	},
	"definition": {
		"dataaccess": {
			"id": {"type": "STRING", "placement": "ATTRIB"},
			"top": {"type": "STRING", "placement": "CHILD"},
			"bottom": {"type": "STRING", "placement": "CHILD"},
			"parameters": {"type": "STRING", "placement": "CHILD"}
		}
	}
},
"xPath_xPath": {
	"metadata": {
		"name": "xPath over xPath",
		"conntype": "xpath.xPath",
		"datype": "xPath",
		"group": "XPATH",
		"groupdesc": "XPATH Queries"
	},
	"definition": {
		"connection": {
			"dataFile": {"type": "STRING", "placement": "CHILD"}
		},
		"dataaccess": {
			"id": {"type": "STRING", "placement": "ATTRIB"},
			"access": {"type": "STRING", "placement": "ATTRIB"},
			"parameters": {"type": "ARRAY", "placement": "CHILD"},
			"output": {"type": "ARRAY", "placement": "CHILD"},
			"columns": {"type": "ARRAY", "placement": "CHILD"},
			"query": {"type": "STRING", "placement": "CHILD"},
			"connection": {"type": "STRING", "placement": "ATTRIB"},
			"cache": {"type": "BOOLEAN", "placement": "ATTRIB"},
			"cacheDuration": {"type": "NUMERIC", "placement": "ATTRIB"}
		}
	}
}
}