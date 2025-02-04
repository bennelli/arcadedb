/* Generated by:  JJTree&ParserGeneratorCC: Do not edit this line. SqlParserConstants.java */
package com.arcadedb.query.sql.parser;


/**
 * Token literal values and constants.
 * Generated by com.helger.pgcc.output.java.OtherFilesGenJava#start()
 */
public interface SqlParserConstants {

  /** End of File. */
  int EOF = 0;
  /** RegularExpression Id. */
  int FORMAL_COMMENT = 8;
  /** RegularExpression Id. */
  int MULTI_LINE_COMMENT = 9;
  /** RegularExpression Id. */
  int SELECT = 11;
  /** RegularExpression Id. */
  int TRAVERSE = 12;
  /** RegularExpression Id. */
  int MATCH = 13;
  /** RegularExpression Id. */
  int INSERT = 14;
  /** RegularExpression Id. */
  int CREATE = 15;
  /** RegularExpression Id. */
  int DELETE = 16;
  /** RegularExpression Id. */
  int DOCUMENT = 17;
  /** RegularExpression Id. */
  int VERTEX = 18;
  /** RegularExpression Id. */
  int EDGE = 19;
  /** RegularExpression Id. */
  int UPDATE = 20;
  /** RegularExpression Id. */
  int UPSERT = 21;
  /** RegularExpression Id. */
  int FROM = 22;
  /** RegularExpression Id. */
  int TO = 23;
  /** RegularExpression Id. */
  int WHERE = 24;
  /** RegularExpression Id. */
  int WHILE = 25;
  /** RegularExpression Id. */
  int INTO = 26;
  /** RegularExpression Id. */
  int VALUE = 27;
  /** RegularExpression Id. */
  int VALUES = 28;
  /** RegularExpression Id. */
  int SET = 29;
  /** RegularExpression Id. */
  int ADD = 30;
  /** RegularExpression Id. */
  int PUT = 31;
  /** RegularExpression Id. */
  int MERGE = 32;
  /** RegularExpression Id. */
  int CONTENT = 33;
  /** RegularExpression Id. */
  int REMOVE = 34;
  /** RegularExpression Id. */
  int INCREMENT = 35;
  /** RegularExpression Id. */
  int AND = 36;
  /** RegularExpression Id. */
  int OR = 37;
  /** RegularExpression Id. */
  int NULL = 38;
  /** RegularExpression Id. */
  int DEFINED = 39;
  /** RegularExpression Id. */
  int ORDER = 40;
  /** RegularExpression Id. */
  int GROUP = 41;
  /** RegularExpression Id. */
  int BY = 42;
  /** RegularExpression Id. */
  int LIMIT = 43;
  /** RegularExpression Id. */
  int SKIP2 = 44;
  /** RegularExpression Id. */
  int ERROR2 = 45;
  /** RegularExpression Id. */
  int OFFSET = 46;
  /** RegularExpression Id. */
  int TIMEOUT = 47;
  /** RegularExpression Id. */
  int ASC = 48;
  /** RegularExpression Id. */
  int AS = 49;
  /** RegularExpression Id. */
  int DESC = 50;
  /** RegularExpression Id. */
  int RETURN = 51;
  /** RegularExpression Id. */
  int BEFORE = 52;
  /** RegularExpression Id. */
  int AFTER = 53;
  /** RegularExpression Id. */
  int RECORD = 54;
  /** RegularExpression Id. */
  int WAIT = 55;
  /** RegularExpression Id. */
  int RETRY = 56;
  /** RegularExpression Id. */
  int LET = 57;
  /** RegularExpression Id. */
  int CACHE = 58;
  /** RegularExpression Id. */
  int CYCLE = 59;
  /** RegularExpression Id. */
  int NOLIMIT = 60;
  /** RegularExpression Id. */
  int NOCYCLE = 61;
  /** RegularExpression Id. */
  int UNSAFE = 62;
  /** RegularExpression Id. */
  int STRATEGY = 63;
  /** RegularExpression Id. */
  int DEPTH_FIRST = 64;
  /** RegularExpression Id. */
  int BREADTH_FIRST = 65;
  /** RegularExpression Id. */
  int NEAR = 66;
  /** RegularExpression Id. */
  int WITHIN = 67;
  /** RegularExpression Id. */
  int UNWIND = 68;
  /** RegularExpression Id. */
  int MAXDEPTH = 69;
  /** RegularExpression Id. */
  int MINDEPTH = 70;
  /** RegularExpression Id. */
  int TYPE = 71;
  /** RegularExpression Id. */
  int SUPERTYPE = 72;
  /** RegularExpression Id. */
  int TYPES = 73;
  /** RegularExpression Id. */
  int SUPERTYPES = 74;
  /** RegularExpression Id. */
  int EXCEPTION = 75;
  /** RegularExpression Id. */
  int PROFILE = 76;
  /** RegularExpression Id. */
  int STORAGE = 77;
  /** RegularExpression Id. */
  int ON = 78;
  /** RegularExpression Id. */
  int OFF = 79;
  /** RegularExpression Id. */
  int TRUNCATE = 80;
  /** RegularExpression Id. */
  int POLYMORPHIC = 81;
  /** RegularExpression Id. */
  int FIND = 82;
  /** RegularExpression Id. */
  int REFERENCES = 83;
  /** RegularExpression Id. */
  int EXTENDS = 84;
  /** RegularExpression Id. */
  int BACKUP = 85;
  /** RegularExpression Id. */
  int BUCKETS = 86;
  /** RegularExpression Id. */
  int ABSTRACT = 87;
  /** RegularExpression Id. */
  int ALTER = 88;
  /** RegularExpression Id. */
  int NAME = 89;
  /** RegularExpression Id. */
  int ADDBUCKET = 90;
  /** RegularExpression Id. */
  int REMOVEBUCKET = 91;
  /** RegularExpression Id. */
  int DROP = 92;
  /** RegularExpression Id. */
  int PROPERTY = 93;
  /** RegularExpression Id. */
  int FORCE = 94;
  /** RegularExpression Id. */
  int SCHEMA = 95;
  /** RegularExpression Id. */
  int INDEX = 96;
  /** RegularExpression Id. */
  int NULL_STRATEGY = 97;
  /** RegularExpression Id. */
  int ENGINE = 98;
  /** RegularExpression Id. */
  int REBUILD = 99;
  /** RegularExpression Id. */
  int ID = 100;
  /** RegularExpression Id. */
  int FORMAT = 101;
  /** RegularExpression Id. */
  int OVERWRITE = 102;
  /** RegularExpression Id. */
  int EXPORT = 103;
  /** RegularExpression Id. */
  int IMPORT = 104;
  /** RegularExpression Id. */
  int DATABASE = 105;
  /** RegularExpression Id. */
  int OPTIMIZE = 106;
  /** RegularExpression Id. */
  int LINK = 107;
  /** RegularExpression Id. */
  int INVERSE = 108;
  /** RegularExpression Id. */
  int EXPLAIN = 109;
  /** RegularExpression Id. */
  int GRANT = 110;
  /** RegularExpression Id. */
  int REVOKE = 111;
  /** RegularExpression Id. */
  int READ = 112;
  /** RegularExpression Id. */
  int EXECUTE = 113;
  /** RegularExpression Id. */
  int ALL = 114;
  /** RegularExpression Id. */
  int NONE = 115;
  /** RegularExpression Id. */
  int FUNCTION = 116;
  /** RegularExpression Id. */
  int PARAMETERS = 117;
  /** RegularExpression Id. */
  int IDEMPOTENT = 118;
  /** RegularExpression Id. */
  int LANGUAGE = 119;
  /** RegularExpression Id. */
  int BEGIN = 120;
  /** RegularExpression Id. */
  int COMMIT = 121;
  /** RegularExpression Id. */
  int ROLLBACK = 122;
  /** RegularExpression Id. */
  int IF = 123;
  /** RegularExpression Id. */
  int ELSE = 124;
  /** RegularExpression Id. */
  int CONTINUE = 125;
  /** RegularExpression Id. */
  int FAIL = 126;
  /** RegularExpression Id. */
  int ISOLATION = 127;
  /** RegularExpression Id. */
  int SLEEP = 128;
  /** RegularExpression Id. */
  int CONSOLE = 129;
  /** RegularExpression Id. */
  int BLOB = 130;
  /** RegularExpression Id. */
  int SHARED = 131;
  /** RegularExpression Id. */
  int DEFAULT_ = 132;
  /** RegularExpression Id. */
  int START = 133;
  /** RegularExpression Id. */
  int OPTIONAL = 134;
  /** RegularExpression Id. */
  int COUNT = 135;
  /** RegularExpression Id. */
  int DISTINCT = 136;
  /** RegularExpression Id. */
  int HA = 137;
  /** RegularExpression Id. */
  int STATUS = 138;
  /** RegularExpression Id. */
  int SERVER = 139;
  /** RegularExpression Id. */
  int SYNC = 140;
  /** RegularExpression Id. */
  int EXISTS = 141;
  /** RegularExpression Id. */
  int FOREACH = 142;
  /** RegularExpression Id. */
  int MOVE = 143;
  /** RegularExpression Id. */
  int DEPTH_ALIAS = 144;
  /** RegularExpression Id. */
  int PATH_ALIAS = 145;
  /** RegularExpression Id. */
  int IDENTIFIED = 146;
  /** RegularExpression Id. */
  int RID = 147;
  /** RegularExpression Id. */
  int SYSTEM = 148;
  /** RegularExpression Id. */
  int THIS = 149;
  /** RegularExpression Id. */
  int RECORD_ATTRIBUTE = 150;
  /** RegularExpression Id. */
  int RID_ATTR = 151;
  /** RegularExpression Id. */
  int RID_STRING = 152;
  /** RegularExpression Id. */
  int OUT_ATTR = 153;
  /** RegularExpression Id. */
  int IN_ATTR = 154;
  /** RegularExpression Id. */
  int TYPE_ATTR = 155;
  /** RegularExpression Id. */
  int RID_ID_ATTR = 156;
  /** RegularExpression Id. */
  int RID_POS_ATTR = 157;
  /** RegularExpression Id. */
  int FIELDS_ATTR = 158;
  /** RegularExpression Id. */
  int INTEGER_LITERAL = 159;
  /** RegularExpression Id. */
  int DECIMAL_LITERAL = 160;
  /** RegularExpression Id. */
  int HEX_LITERAL = 161;
  /** RegularExpression Id. */
  int OCTAL_LITERAL = 162;
  /** RegularExpression Id. */
  int FLOATING_POINT_LITERAL = 163;
  /** RegularExpression Id. */
  int DECIMAL_FLOATING_POINT_LITERAL = 164;
  /** RegularExpression Id. */
  int DECIMAL_EXPONENT = 165;
  /** RegularExpression Id. */
  int HEXADECIMAL_FLOATING_POINT_LITERAL = 166;
  /** RegularExpression Id. */
  int HEXADECIMAL_EXPONENT = 167;
  /** RegularExpression Id. */
  int CHARACTER_LITERAL = 168;
  /** RegularExpression Id. */
  int STRING_LITERAL = 169;
  /** RegularExpression Id. */
  int INTEGER_RANGE = 170;
  /** RegularExpression Id. */
  int ELLIPSIS_INTEGER_RANGE = 171;
  /** RegularExpression Id. */
  int TRUE = 172;
  /** RegularExpression Id. */
  int FALSE = 173;
  /** RegularExpression Id. */
  int LPAREN = 174;
  /** RegularExpression Id. */
  int RPAREN = 175;
  /** RegularExpression Id. */
  int LBRACE = 176;
  /** RegularExpression Id. */
  int RBRACE = 177;
  /** RegularExpression Id. */
  int LBRACKET = 178;
  /** RegularExpression Id. */
  int RBRACKET = 179;
  /** RegularExpression Id. */
  int SEMICOLON = 180;
  /** RegularExpression Id. */
  int COMMA = 181;
  /** RegularExpression Id. */
  int DOT = 182;
  /** RegularExpression Id. */
  int AT = 183;
  /** RegularExpression Id. */
  int DOLLAR = 184;
  /** RegularExpression Id. */
  int BACKTICK = 185;
  /** RegularExpression Id. */
  int EQ = 186;
  /** RegularExpression Id. */
  int EQEQ = 187;
  /** RegularExpression Id. */
  int LT = 188;
  /** RegularExpression Id. */
  int GT = 189;
  /** RegularExpression Id. */
  int BANG = 190;
  /** RegularExpression Id. */
  int TILDE = 191;
  /** RegularExpression Id. */
  int HOOK = 192;
  /** RegularExpression Id. */
  int COLON = 193;
  /** RegularExpression Id. */
  int LE = 194;
  /** RegularExpression Id. */
  int GE = 195;
  /** RegularExpression Id. */
  int NE = 196;
  /** RegularExpression Id. */
  int NEQ = 197;
  /** RegularExpression Id. */
  int SC_OR = 198;
  /** RegularExpression Id. */
  int SC_AND = 199;
  /** RegularExpression Id. */
  int INCR = 200;
  /** RegularExpression Id. */
  int DECR = 201;
  /** RegularExpression Id. */
  int PLUS = 202;
  /** RegularExpression Id. */
  int MINUS = 203;
  /** RegularExpression Id. */
  int STAR = 204;
  /** RegularExpression Id. */
  int SLASH = 205;
  /** RegularExpression Id. */
  int BIT_AND = 206;
  /** RegularExpression Id. */
  int NULL_COALESCING = 207;
  /** RegularExpression Id. */
  int BIT_OR = 208;
  /** RegularExpression Id. */
  int XOR = 209;
  /** RegularExpression Id. */
  int REM = 210;
  /** RegularExpression Id. */
  int LSHIFT = 211;
  /** RegularExpression Id. */
  int PLUSASSIGN = 212;
  /** RegularExpression Id. */
  int MINUSASSIGN = 213;
  /** RegularExpression Id. */
  int STARASSIGN = 214;
  /** RegularExpression Id. */
  int SLASHASSIGN = 215;
  /** RegularExpression Id. */
  int ANDASSIGN = 216;
  /** RegularExpression Id. */
  int ORASSIGN = 217;
  /** RegularExpression Id. */
  int XORASSIGN = 218;
  /** RegularExpression Id. */
  int REMASSIGN = 219;
  /** RegularExpression Id. */
  int LSHIFTASSIGN = 220;
  /** RegularExpression Id. */
  int RSIGNEDSHIFTASSIGN = 221;
  /** RegularExpression Id. */
  int RUNSIGNEDSHIFTASSIGN = 222;
  /** RegularExpression Id. */
  int RSHIFT = 223;
  /** RegularExpression Id. */
  int RUNSIGNEDSHIFT = 224;
  /** RegularExpression Id. */
  int ELLIPSIS = 225;
  /** RegularExpression Id. */
  int RANGE = 226;
  /** RegularExpression Id. */
  int NOT = 227;
  /** RegularExpression Id. */
  int IN = 228;
  /** RegularExpression Id. */
  int LIKE = 229;
  /** RegularExpression Id. */
  int IS = 230;
  /** RegularExpression Id. */
  int BETWEEN = 231;
  /** RegularExpression Id. */
  int CONTAINS = 232;
  /** RegularExpression Id. */
  int CONTAINSALL = 233;
  /** RegularExpression Id. */
  int CONTAINSANY = 234;
  /** RegularExpression Id. */
  int CONTAINSKEY = 235;
  /** RegularExpression Id. */
  int CONTAINSVALUE = 236;
  /** RegularExpression Id. */
  int CONTAINSTEXT = 237;
  /** RegularExpression Id. */
  int MATCHES = 238;
  /** RegularExpression Id. */
  int KEY = 239;
  /** RegularExpression Id. */
  int INSTANCEOF = 240;
  /** RegularExpression Id. */
  int BUCKET = 241;
  /** RegularExpression Id. */
  int IDENTIFIER = 242;
  /** RegularExpression Id. */
  int QUOTED_IDENTIFIER = 243;
  /** RegularExpression Id. */
  int INDEX_COLON = 244;
  /** RegularExpression Id. */
  int INDEXVALUES_IDENTIFIER = 245;
  /** RegularExpression Id. */
  int INDEXVALUESASC_IDENTIFIER = 246;
  /** RegularExpression Id. */
  int INDEXVALUESDESC_IDENTIFIER = 247;
  /** RegularExpression Id. */
  int BUCKET_IDENTIFIER = 248;
  /** RegularExpression Id. */
  int BUCKET_NUMBER_IDENTIFIER = 249;
  /** RegularExpression Id. */
  int HTTP_URL = 250;
  /** RegularExpression Id. */
  int HTTPS_URL = 251;
  /** RegularExpression Id. */
  int FILE_URL = 252;
  /** RegularExpression Id. */
  int CLASSPATH_URL = 253;
  /** RegularExpression Id. */
  int SCHEMA_IDENTIFIER = 254;
  /** RegularExpression Id. */
  int LETTER = 255;
  /** RegularExpression Id. */
  int PART_LETTER = 256;

  /** Lexical state. */
  int DEFAULT = 0;
  /** Lexical state. */
  int IN_FORMAL_COMMENT = 1;
  /** Lexical state. */
  int IN_MULTI_LINE_COMMENT = 2;

  /** Literal token values. */
  String[] tokenImage = {
    "<EOF>",
    "\" \"",
    "\"\\t\"",
    "\"\\n\"",
    "\"\\r\"",
    "\"\\f\"",
    "<token of kind 6>",
    "\"/*\"",
    "\"*/\"",
    "\"*/\"",
    "<token of kind 10>",
    "<SELECT>",
    "<TRAVERSE>",
    "<MATCH>",
    "<INSERT>",
    "<CREATE>",
    "<DELETE>",
    "<DOCUMENT>",
    "<VERTEX>",
    "<EDGE>",
    "<UPDATE>",
    "<UPSERT>",
    "<FROM>",
    "<TO>",
    "<WHERE>",
    "<WHILE>",
    "<INTO>",
    "<VALUE>",
    "<VALUES>",
    "<SET>",
    "<ADD>",
    "<PUT>",
    "<MERGE>",
    "<CONTENT>",
    "<REMOVE>",
    "<INCREMENT>",
    "<AND>",
    "<OR>",
    "<NULL>",
    "<DEFINED>",
    "<ORDER>",
    "<GROUP>",
    "<BY>",
    "<LIMIT>",
    "<SKIP2>",
    "<ERROR2>",
    "<OFFSET>",
    "<TIMEOUT>",
    "<ASC>",
    "<AS>",
    "<DESC>",
    "<RETURN>",
    "<BEFORE>",
    "<AFTER>",
    "<RECORD>",
    "<WAIT>",
    "<RETRY>",
    "<LET>",
    "<CACHE>",
    "<CYCLE>",
    "<NOLIMIT>",
    "<NOCYCLE>",
    "<UNSAFE>",
    "<STRATEGY>",
    "<DEPTH_FIRST>",
    "<BREADTH_FIRST>",
    "<NEAR>",
    "<WITHIN>",
    "<UNWIND>",
    "<MAXDEPTH>",
    "<MINDEPTH>",
    "<TYPE>",
    "<SUPERTYPE>",
    "<TYPES>",
    "<SUPERTYPES>",
    "<EXCEPTION>",
    "<PROFILE>",
    "<STORAGE>",
    "<ON>",
    "<OFF>",
    "<TRUNCATE>",
    "<POLYMORPHIC>",
    "<FIND>",
    "<REFERENCES>",
    "<EXTENDS>",
    "<BACKUP>",
    "<BUCKETS>",
    "<ABSTRACT>",
    "<ALTER>",
    "<NAME>",
    "<ADDBUCKET>",
    "<REMOVEBUCKET>",
    "<DROP>",
    "<PROPERTY>",
    "<FORCE>",
    "<SCHEMA>",
    "<INDEX>",
    "<NULL_STRATEGY>",
    "<ENGINE>",
    "<REBUILD>",
    "<ID>",
    "<FORMAT>",
    "<OVERWRITE>",
    "<EXPORT>",
    "<IMPORT>",
    "<DATABASE>",
    "<OPTIMIZE>",
    "<LINK>",
    "<INVERSE>",
    "<EXPLAIN>",
    "<GRANT>",
    "<REVOKE>",
    "<READ>",
    "<EXECUTE>",
    "<ALL>",
    "<NONE>",
    "<FUNCTION>",
    "<PARAMETERS>",
    "<IDEMPOTENT>",
    "<LANGUAGE>",
    "<BEGIN>",
    "<COMMIT>",
    "<ROLLBACK>",
    "<IF>",
    "<ELSE>",
    "<CONTINUE>",
    "<FAIL>",
    "<ISOLATION>",
    "<SLEEP>",
    "<CONSOLE>",
    "<BLOB>",
    "<SHARED>",
    "<DEFAULT_>",
    "<START>",
    "<OPTIONAL>",
    "<COUNT>",
    "<DISTINCT>",
    "<HA>",
    "<STATUS>",
    "<SERVER>",
    "<SYNC>",
    "<EXISTS>",
    "<FOREACH>",
    "<MOVE>",
    "<DEPTH_ALIAS>",
    "<PATH_ALIAS>",
    "<IDENTIFIED>",
    "<RID>",
    "<SYSTEM>",
    "<THIS>",
    "<RECORD_ATTRIBUTE>",
    "<RID_ATTR>",
    "<RID_STRING>",
    "<OUT_ATTR>",
    "<IN_ATTR>",
    "<TYPE_ATTR>",
    "<RID_ID_ATTR>",
    "<RID_POS_ATTR>",
    "<FIELDS_ATTR>",
    "<INTEGER_LITERAL>",
    "<DECIMAL_LITERAL>",
    "<HEX_LITERAL>",
    "<OCTAL_LITERAL>",
    "<FLOATING_POINT_LITERAL>",
    "<DECIMAL_FLOATING_POINT_LITERAL>",
    "<DECIMAL_EXPONENT>",
    "<HEXADECIMAL_FLOATING_POINT_LITERAL>",
    "<HEXADECIMAL_EXPONENT>",
    "<CHARACTER_LITERAL>",
    "<STRING_LITERAL>",
    "<INTEGER_RANGE>",
    "<ELLIPSIS_INTEGER_RANGE>",
    "<TRUE>",
    "<FALSE>",
    "\"(\"",
    "\")\"",
    "\"{\"",
    "\"}\"",
    "\"[\"",
    "\"]\"",
    "\";\"",
    "\",\"",
    "\".\"",
    "\"@\"",
    "\"$\"",
    "\"`\"",
    "\"=\"",
    "\"==\"",
    "\"<\"",
    "\">\"",
    "\"!\"",
    "\"~\"",
    "\"?\"",
    "\":\"",
    "\"<=\"",
    "\">=\"",
    "\"!=\"",
    "\"<>\"",
    "\"||\"",
    "\"&&\"",
    "\"++\"",
    "\"--\"",
    "\"+\"",
    "\"-\"",
    "\"*\"",
    "\"/\"",
    "\"&\"",
    "\"??\"",
    "\"|\"",
    "\"^\"",
    "\"%\"",
    "\"<<\"",
    "\"+=\"",
    "\"-=\"",
    "\"*=\"",
    "\"/=\"",
    "\"&=\"",
    "\"|=\"",
    "\"^=\"",
    "\"%=\"",
    "\"<<=\"",
    "\">>=\"",
    "\">>>=\"",
    "\">>\"",
    "\">>>\"",
    "\"...\"",
    "\"..\"",
    "<NOT>",
    "<IN>",
    "<LIKE>",
    "<IS>",
    "<BETWEEN>",
    "<CONTAINS>",
    "<CONTAINSALL>",
    "<CONTAINSANY>",
    "<CONTAINSKEY>",
    "<CONTAINSVALUE>",
    "<CONTAINSTEXT>",
    "<MATCHES>",
    "<KEY>",
    "<INSTANCEOF>",
    "<BUCKET>",
    "<IDENTIFIER>",
    "<QUOTED_IDENTIFIER>",
    "<INDEX_COLON>",
    "<INDEXVALUES_IDENTIFIER>",
    "<INDEXVALUESASC_IDENTIFIER>",
    "<INDEXVALUESDESC_IDENTIFIER>",
    "<BUCKET_IDENTIFIER>",
    "<BUCKET_NUMBER_IDENTIFIER>",
    "<HTTP_URL>",
    "<HTTPS_URL>",
    "<FILE_URL>",
    "<CLASSPATH_URL>",
    "<SCHEMA_IDENTIFIER>",
    "<LETTER>",
    "<PART_LETTER>",
    "\"#\"",
    "\"__@recordmap@___\"",
  };

}
