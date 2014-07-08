/***
 *   Copyright 2014 Rackspace US, Inc.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.rackspace.com.papi.components.checker.wadl

import scala.xml._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers._

import com.rackspace.com.papi.components.checker.Config

@RunWith(classOf[JUnitRunner])
class WADLCheckerMetaSpec extends BaseCheckerSpec {

  //
  //  Register some common prefixes, you'll need the for XPath
  //  assertions.
  //
  register ("xsd", "http://www.w3.org/2001/XMLSchema")
  register ("wadl","http://wadl.dev.java.net/2009/02")
  register ("chk","http://www.rackspace.com/repose/wadl/checker")


  feature ("The WADLCheckerBuilder can correctly set metadata") {

    type Dep  = (String, NodeSeq) /* (Base URL, XML Source */
    type WADL = (String, Dep)    /* Description (Base URL, WADL Source) */
    type Deps = List[Dep] /* A list of dependecies */

    val testWADLs : Map[WADL, Deps] = Map(
        ("WADL with no deps",
        ("test://path/to/test/mywadl.wadl",
         <application xmlns="http://wadl.dev.java.net/2009/02" xmlns:rax="http://docs.rackspace.com/api"
                   xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:tst="test://schema/a">
        <grammars>
           <schema elementFormDefault="qualified"
                   attributeFormDefault="unqualified"
                   xmlns="http://www.w3.org/2001/XMLSchema"
                   xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                   targetNamespace="test://schema/a">
              <simpleType name="yesno">
                 <restriction base="xsd:string">
                     <enumeration value="yes"/>
                     <enumeration value="no"/>
                 </restriction>
             </simpleType>
           </schema>
        </grammars>
        <resources base="https://test.api.openstack.com">
          <resource path="/a" rax:roles="a:admin">
            <method name="PUT" rax:roles="a:observer"/>
            <resource path="/b" rax:roles="b:creator">
              <method name="POST"/>
              <method name="PUT" rax:roles="b:observer"/>
              <method name="DELETE" rax:roles="b:observer b:admin"/>
              <method name="GET"  rax:roles="#all"/>
            </resource>
            <resource path="{yn}">
              <param name="yn" style="template" type="tst:yesno"/>
              <method name="POST"/>
              <method name="PUT" rax:roles="b:observer"/>
            </resource>
          </resource>
        </resources>
      </application>
      )) -> List(),
      ("WADL with schema reference in grammar",
      ("test://path/to/test/mywadl.wadl",
         <application xmlns="http://wadl.dev.java.net/2009/02" xmlns:rax="http://docs.rackspace.com/api"
                   xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:tst="test://schema/a">
        <grammars>
           <include href="xsd/mytest.xsd"/>
        </grammars>
        <resources base="https://test.api.openstack.com">
          <resource path="/a" rax:roles="a:admin">
            <method name="PUT" rax:roles="a:observer"/>
            <resource path="/b" rax:roles="b:creator">
              <method name="POST"/>
              <method name="PUT" rax:roles="b:observer"/>
              <method name="DELETE" rax:roles="b:observer b:admin"/>
              <method name="GET"  rax:roles="#all"/>
            </resource>
            <resource path="{yn}">
              <param name="yn" style="template" type="tst:yesno"/>
              <method name="POST"/>
              <method name="PUT" rax:roles="b:observer"/>
            </resource>
          </resource>
        </resources>
      </application>
      )) -> List(("test://path/to/test/xsd/mytest.xsd",
                  <schema elementFormDefault="qualified"
                   attributeFormDefault="unqualified"
                   xmlns="http://www.w3.org/2001/XMLSchema"
                   xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                   targetNamespace="test://schema/a">
              <simpleType name="yesno">
                 <restriction base="xsd:string">
                     <enumeration value="yes"/>
                     <enumeration value="no"/>
                 </restriction>
             </simpleType>
           </schema>)),
      ("WADL with schema reference in grammar. The schema itself includes a reference.",
      ("test://path/to/test/mywadl.wadl",
         <application xmlns="http://wadl.dev.java.net/2009/02" xmlns:rax="http://docs.rackspace.com/api"
                   xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:tst="test://schema/a">
        <grammars>
           <include href="xsd/mytest.xsd"/>
        </grammars>
        <resources base="https://test.api.openstack.com">
          <resource path="/a" rax:roles="a:admin">
            <method name="PUT" rax:roles="a:observer"/>
            <resource path="/b" rax:roles="b:creator">
              <method name="POST"/>
              <method name="PUT" rax:roles="b:observer"/>
              <method name="DELETE" rax:roles="b:observer b:admin"/>
              <method name="GET"  rax:roles="#all"/>
            </resource>
            <resource path="{yn}">
              <param name="yn" style="template" type="tst:yesno"/>
              <method name="POST"/>
              <method name="PUT" rax:roles="b:observer"/>
            </resource>
          </resource>
        </resources>
      </application>
      )) -> List(("test://path/to/test/xsd/mytest.xsd",
                  <schema elementFormDefault="qualified"
                   attributeFormDefault="unqualified"
                   xmlns="http://www.w3.org/2001/XMLSchema"
                   xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                   targetNamespace="test://schema/a">
                  <include schemaLocation="mytest2.xsd"/>
                  </schema>),
               ("test://path/to/test/xsd/mytest2.xsd",
                  <schema elementFormDefault="qualified"
                   attributeFormDefault="unqualified"
                   xmlns="http://www.w3.org/2001/XMLSchema"
                   xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                   targetNamespace="test://schema/a">
              <simpleType name="yesno">
                 <restriction base="xsd:string">
                     <enumeration value="yes"/>
                     <enumeration value="no"/>
                 </restriction>
             </simpleType>
           </schema>
              )),
      ("WADL with inline XSD grammar. The schema includes a reference.",
      ("test://path/to/test/mywadl.wadl",
         <application xmlns="http://wadl.dev.java.net/2009/02" xmlns:rax="http://docs.rackspace.com/api"
                   xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:tst="test://schema/a">
        <grammars>
             <schema elementFormDefault="qualified"
                   attributeFormDefault="unqualified"
                   xmlns="http://www.w3.org/2001/XMLSchema"
                   xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                   targetNamespace="test://schema/a">
                  <include schemaLocation="xsd/mytest2.xsd"/>
             </schema>
        </grammars>
        <resources base="https://test.api.openstack.com">
          <resource path="/a" rax:roles="a:admin">
            <method name="PUT" rax:roles="a:observer"/>
            <resource path="/b" rax:roles="b:creator">
              <method name="POST"/>
              <method name="PUT" rax:roles="b:observer"/>
              <method name="DELETE" rax:roles="b:observer b:admin"/>
              <method name="GET"  rax:roles="#all"/>
            </resource>
            <resource path="{yn}">
              <param name="yn" style="template" type="tst:yesno"/>
              <method name="POST"/>
              <method name="PUT" rax:roles="b:observer"/>
            </resource>
          </resource>
        </resources>
      </application>
      )) -> List(("test://path/to/test/xsd/mytest2.xsd",
                  <schema elementFormDefault="qualified"
                   attributeFormDefault="unqualified"
                   xmlns="http://www.w3.org/2001/XMLSchema"
                   xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                   targetNamespace="test://schema/a">
              <simpleType name="yesno">
                 <restriction base="xsd:string">
                     <enumeration value="yes"/>
                     <enumeration value="no"/>
                 </restriction>
             </simpleType>
           </schema>
              )),
      ("WADL with schema reference in grammar. The schema itself includes a reference, the other schema imports one.",
      ("test://path/to/test/mywadl.wadl",
         <application xmlns="http://wadl.dev.java.net/2009/02" xmlns:rax="http://docs.rackspace.com/api"
                   xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:tst="test://schema/a">
        <grammars>
           <include href="xsd/mytest.xsd"/>
        </grammars>
        <resources base="https://test.api.openstack.com">
          <resource path="/a" rax:roles="a:admin">
            <method name="PUT" rax:roles="a:observer"/>
            <resource path="/b" rax:roles="b:creator">
              <method name="POST"/>
              <method name="PUT" rax:roles="b:observer"/>
              <method name="DELETE" rax:roles="b:observer b:admin"/>
              <method name="GET"  rax:roles="#all"/>
            </resource>
            <resource path="{yn}">
              <param name="yn" style="template" type="tst:yesno"/>
              <method name="POST"/>
              <method name="PUT" rax:roles="b:observer"/>
            </resource>
          </resource>
        </resources>
      </application>
      )) -> List(("test://path/to/test/xsd/mytest.xsd",
                  <schema elementFormDefault="qualified"
                   attributeFormDefault="unqualified"
                   xmlns="http://www.w3.org/2001/XMLSchema"
                   xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                   targetNamespace="test://schema/a">
                  <include schemaLocation="mytest2.xsd"/>
                  </schema>),
               ("test://path/to/test/xsd/mytest2.xsd",
                  <schema elementFormDefault="qualified"
                   attributeFormDefault="unqualified"
                   xmlns="http://www.w3.org/2001/XMLSchema"
                   xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                   targetNamespace="test://schema/a">
                 <import namespace="test://schema/b" schemaLocation="mytest3.xsd"/>
              <simpleType name="yesno">
                 <restriction base="xsd:string">
                     <enumeration value="yes"/>
                     <enumeration value="no"/>
                 </restriction>
             </simpleType>
           </schema>
              ),
             ("test://path/to/test/xsd/mytest3.xsd",
                  <schema elementFormDefault="qualified"
                   attributeFormDefault="unqualified"
                   xmlns="http://www.w3.org/2001/XMLSchema"
                   xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                   targetNamespace="test://schema/b">
              <simpleType name="yes">
                 <restriction base="xsd:string">
                     <enumeration value="yes"/>
                 </restriction>
             </simpleType>
           </schema>
              )),
        ("WADL referencing resource type in another WADL",
        ("test://path/to/test/mywadl.wadl",
         <application xmlns="http://wadl.dev.java.net/2009/02" xmlns:rax="http://docs.rackspace.com/api"
                   xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:tst="test://schema/a">
        <grammars>
           <schema elementFormDefault="qualified"
                   attributeFormDefault="unqualified"
                   xmlns="http://www.w3.org/2001/XMLSchema"
                   xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                   targetNamespace="test://schema/a">
              <simpleType name="yesno">
                 <restriction base="xsd:string">
                     <enumeration value="yes"/>
                     <enumeration value="no"/>
                 </restriction>
             </simpleType>
           </schema>
        </grammars>
        <resources base="https://test.api.openstack.com">
          <resource path="/a" type="other.wadl#methods" rax:roles="a:admin">
            <resource path="/b" rax:roles="b:creator">
              <method name="POST"/>
              <method name="PUT" rax:roles="b:observer"/>
              <method name="DELETE" rax:roles="b:observer b:admin"/>
              <method name="GET"  rax:roles="#all"/>
            </resource>
            <resource path="{yn}">
              <param name="yn" style="template" type="tst:yesno"/>
              <method name="POST"/>
              <method name="PUT" rax:roles="b:observer"/>
            </resource>
          </resource>
        </resources>
      </application>
      )) -> List(("test://path/to/test/other.wadl",
         <application xmlns="http://wadl.dev.java.net/2009/02" xmlns:rax="http://docs.rackspace.com/api"
                   xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:tst="test://schema/a">
        <resource_type id="methods">
            <method name="PUT" rax:roles="a:observer"/>
        </resource_type>
      </application>)),
      ("WADL referencing resource type in another WADL, which references a method in another",
        ("test://path/to/test/mywadl.wadl",
         <application xmlns="http://wadl.dev.java.net/2009/02" xmlns:rax="http://docs.rackspace.com/api"
                   xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:tst="test://schema/a">
        <grammars>
           <schema elementFormDefault="qualified"
                   attributeFormDefault="unqualified"
                   xmlns="http://www.w3.org/2001/XMLSchema"
                   xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                   targetNamespace="test://schema/a">
              <simpleType name="yesno">
                 <restriction base="xsd:string">
                     <enumeration value="yes"/>
                     <enumeration value="no"/>
                 </restriction>
             </simpleType>
           </schema>
        </grammars>
        <resources base="https://test.api.openstack.com">
          <resource path="/a" type="other.wadl#methods" rax:roles="a:admin">
            <resource path="/b" rax:roles="b:creator">
              <method name="POST"/>
              <method name="PUT" rax:roles="b:observer"/>
              <method name="DELETE" rax:roles="b:observer b:admin"/>
              <method name="GET"  rax:roles="#all"/>
            </resource>
            <resource path="{yn}">
              <param name="yn" style="template" type="tst:yesno"/>
              <method name="POST"/>
              <method name="PUT" rax:roles="b:observer"/>
            </resource>
          </resource>
        </resources>
      </application>
      )) -> List(("test://path/to/test/other.wadl",
         <application xmlns="http://wadl.dev.java.net/2009/02" xmlns:rax="http://docs.rackspace.com/api"
                   xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:tst="test://schema/a">
        <resource_type id="methods">
            <method href="other2.wadl#PUTMethod"/>
        </resource_type>
      </application>),
                 ("test://path/to/test/other2.wadl",
         <application xmlns="http://wadl.dev.java.net/2009/02" xmlns:rax="http://docs.rackspace.com/api"
                   xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:tst="test://schema/a">
            <method id="PUTMethod" name="PUT" rax:roles="a:observer"/>
      </application>))
    )

    def clearConfig (c : Config) : Config = {
      c.removeDups = false
      c.validateChecker = true /* We should always validate */
      c.checkWellFormed = false
      c.checkXSDGrammar = false
      c.doXSDGrammarTransform = false
      c.checkJSONGrammar = false
      c.checkElements = false
      c.checkPlainParams = false
      c.enablePreProcessExtension = false
      c.enableIgnoreXSDExtension = false
      c.enableIgnoreJSONSchemaExtension = false
      c.enableMessageExtension = false
      c.enableRaxRolesExtension = false
      c.maskRaxRoles403 = false
      c.joinXPathChecks = false
      c.checkHeaders = false
      c.preserveRequestBody = false

      c
    }

    type MetaTest = (String, Config, String) /* Desc, Config, XPath assertion */
    type MetaTests = List[MetaTest]
    type Assertion = (String, String) /* Desc, XPath assertion */
    type Assertions = List[Assertion]

    def setConfig (setter : (Config) => Unit) : Config = {
      val conf = clearConfig(new Config)
      setter(conf)
      conf
    }

    def checkConfigOptionTrue(option : String, setter : (Config) => Unit) : MetaTest = {
      (s"Metadata $option check should be true if set", setConfig(setter),
       s"/chk:checker/chk:meta/chk:config[@option='$option' and @value='true']")
    }

    def checkConfigOptionFalse(option : String, setter : (Config) => Unit) : MetaTest = {
      (s"Metadata $option check should be false if not set", setConfig(setter),
       s"not(/chk:checker/chk:meta/chk:config[@option='$option']) or /chk:checker/chk:meta/chk:config[@option='$option' and @value='false']")
    }

    def checkConfigOptionMatch(option : String, value: String, setter : (Config) => Unit) : MetaTest = {
      (s"Metadata $option  $value", setConfig(setter),
       s"/chk:checker/chk:meta/chk:config[@option='$option' and @value='$value']")
    }

    //
    // Test for individual configuration flags
    //
    val optionTests :  MetaTests =
      List(checkConfigOptionTrue("enableXSDContentCheck", (c : Config) => c.checkXSDGrammar=true),
           checkConfigOptionFalse("enableXSDContentCheck", (c : Config) => c.checkXSDGrammar=false),
           checkConfigOptionTrue("enableJSONContentCheck", (c : Config) => c.checkJSONGrammar=true),
           checkConfigOptionFalse("enableJSONContentCheck", (c : Config) => c.checkJSONGrammar=false),
           checkConfigOptionTrue("enableXSDTransform", (c : Config) => c.doXSDGrammarTransform=true),
           checkConfigOptionFalse("enableXSDTransform", (c : Config) => c.doXSDGrammarTransform=false),
           checkConfigOptionTrue("enableWellFormCheck", (c : Config) => c.checkWellFormed=true),
           checkConfigOptionFalse("enableWellFormCheck", (c : Config) => c.checkWellFormed=false),
           checkConfigOptionTrue("enableElementCheck", (c : Config) => c.checkElements=true),
           checkConfigOptionFalse("enableElementCheck", (c : Config) => c.checkElements=false),
           checkConfigOptionTrue("enablePlainParamCheck", (c : Config) => c.checkPlainParams=true),
           checkConfigOptionFalse("enablePlainParamCheck", (c : Config) => c.checkPlainParams=false),
           checkConfigOptionTrue("enablePreProcessExtension", (c : Config) => c.enablePreProcessExtension=true),
           checkConfigOptionFalse("enablePreProcessExtension", (c : Config) => c.enablePreProcessExtension=false),
           checkConfigOptionTrue("enableIgnoreXSDExtension", (c : Config) => c.enableIgnoreXSDExtension=true),
           checkConfigOptionFalse("enableIgnoreXSDExtension", (c : Config) => c.enableIgnoreXSDExtension=false),
           checkConfigOptionTrue("enableIgnoreJSONSchemaExtension", (c : Config) => c.enableIgnoreJSONSchemaExtension=true),
           checkConfigOptionFalse("enableIgnoreJSONSchemaExtension", (c : Config) => c.enableIgnoreJSONSchemaExtension=false),
           checkConfigOptionTrue("enableMessageExtension", (c : Config) => c.enableMessageExtension=true),
           checkConfigOptionFalse("enableMessageExtension", (c : Config) => c.enableMessageExtension=false),
           checkConfigOptionTrue("enableHeaderCheck", (c : Config) => c.checkHeaders=true),
           checkConfigOptionFalse("enableHeaderCheck", (c : Config) => c.checkHeaders=false),
           checkConfigOptionTrue("enableRaxRoles", (c : Config) => c.enableRaxRolesExtension=true),
           checkConfigOptionFalse("enableRaxRoles", (c : Config) => c.enableRaxRolesExtension=false),
           checkConfigOptionTrue("enableMaskRaxRoles403", (c : Config)  => {c.enableRaxRolesExtension = true; c.maskRaxRoles403=true} ),
           checkConfigOptionFalse("enableMaskRaxRoles403", (c : Config) => {c.enableRaxRolesExtension = true;  c.maskRaxRoles403=false}),
           checkConfigOptionTrue("enableRemoveDups", (c : Config) => c.removeDups=true),
           checkConfigOptionFalse("enableRemoveDups", (c : Config) => c.removeDups=false),
           checkConfigOptionTrue("enableJoinXPathChecks", (c : Config) => c.joinXPathChecks=true),
           checkConfigOptionFalse("enableJoinXPathChecks", (c : Config) => c.joinXPathChecks=false),
           checkConfigOptionTrue("preserveRequestBody", (c : Config) => {c.joinXPathChecks=true; c.preserveRequestBody=true}),
           checkConfigOptionFalse("preserveRequestBody", (c : Config) => {c.joinXPathChecks=true; c.preserveRequestBody=false}),
           checkConfigOptionMatch("defaultXPathVersion","1",(c : Config) => {c.joinXPathChecks=true; c.xpathVersion=1}),
           checkConfigOptionMatch("defaultXPathVersion","2",(c : Config) => {c.joinXPathChecks=true; c.xpathVersion=2})
         )

    //
    //  These assertions should hold true, regardress of configuration.
    //
    val standardAssertions : Assertions =
      List(("Metadata should contain a user","not(empty(/chk:checker/chk:meta/chk:built-by))"),
           ("Metadata should cantain a creator","not(empty(/chk:checker/chk:meta/chk:created-by))"),
           ("Metadata should cantain a created on date","not(empty(/chk:checker/chk:meta/chk:created-on))"),
           ("Metadata should reference source WADL","/chk:checker/chk:meta/chk:created-from[. = 'test://path/to/test/mywadl.wadl']"))

    val standardTests : MetaTests = for {t <- optionTests
                                         sa <- standardAssertions}
                                    yield (sa._1+" with "+t._1, t._2, sa._2)

    val allConfigTests = optionTests ++ standardTests

    for ( t <- allConfigTests ) {
      for ((w, deps) <- testWADLs) {
        scenario ("The checker for "+w._1+" contains Metadata and "+t._1) {
          for (dep <- deps) {
            register(dep)
          }
          val checker = builder.build (w._2, t._2)
          assert(checker, t._3)
          for (dep <- deps) {
            val durl = dep._1
            assert(checker, s"/chk:checker/chk:meta/chk:created-from[. = '$durl']")
          }
        }
      }
    }
  }
}
