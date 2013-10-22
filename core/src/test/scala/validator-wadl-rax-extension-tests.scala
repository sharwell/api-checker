package com.rackspace.com.papi.components.checker

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import scala.xml._

import com.rackspace.com.papi.components.checker.servlet.RequestAttributes._
import com.rackspace.cloud.api.wadl.Converters._
import Converters._

import org.scalatest.FlatSpec
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import javax.servlet.FilterChain
import java.io.File
import org.mockito.Mockito._


trait RaxRolesBehaviors {
  this: FlatSpec =>

  def base: BaseValidatorSuite = new BaseValidatorSuite()

  val xml = base.goodXML_XSD2

  def response: HttpServletResponse = mock(classOf[HttpServletResponse]);

  def chain: FilterChain = mock(classOf[FilterChain])

  def allowAccess(validator: => Validator, method: => String, path: => String, roles: => List[String]) {
    def request: HttpServletRequest = base.request(method, path, "application/xml", xml, false, Map("X-ROLES" -> roles))
    it should "succeed when " +method+ " on " +path+ " and X-Roles has " + roles + "" in {
      validator.validate(request, response, chain)
    }
  }

  def preventAccess(validator: => Validator, method: => String, path: => String, roles: => List[String]) {
    def request: HttpServletRequest = base.request(method, path, "application/xml", xml, false, Map("X-ROLES" -> roles))
    it should "fail with a 403 when " + method + " on " + path + " and X-Roles has " + roles + "" in {
      base.assertResultFailed(validator.validate(request, response, chain), 403)
    }
  }

  def preventAccessWhenNoXRoles(validator: => Validator, method: => String, path: => String) {
    def request: HttpServletRequest = base.request(method, path, "application/xml", xml, false)
    it should "fail with a 403 when " + method + " on " + path + " and no X-Roles header" in {
      base.assertResultFailed(validator.validate(request, response, chain), 403)
    }
  }

  def allowAccessWhenNoXRoles(validator: => Validator, method: => String, path: => String) {
    def request: HttpServletRequest = base.request(method, path, "application/xml", xml, false)
    it should "succeed when " +method+ " on " +path+ " and no X-Roles" in {
      validator.validate(request, response, chain)
    }
  }
}

@RunWith(classOf[JUnitRunner])
class GivenAWadlWithRolesAtMethodLevel extends FlatSpec with RaxRolesBehaviors {

  val localWADLURI = (new File(System.getProperty("user.dir"), "mywadl.wadl")).toURI.toString
  val validator = Validator((localWADLURI,
    <application xmlns="http://wadl.dev.java.net/2009/02"
                 xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                 xmlns:tst="http://www.rackspace.com/repose/wadl/checker/step/test"
                 xmlns:rax="http://docs.rackspace.com/api">
      <resources base="https://test.api.openstack.com">
        <resource path="/a">
          <method name="POST" rax:roles="foo:creator">
            <request>
              <representation mediaType="application/xml"/>
            </request>
          </method>
          <method name="GET" rax:roles="foo:observer">
            <request>
              <representation mediaType="application/xml"/>
            </request>
          </method>
          <method name="PUT">
            <request>
              <representation mediaType="application/xml"/>
            </request>
          </method>
          <method name="DELETE" rax:roles="foo:observer foo:creator">
            <request>
              <representation mediaType="application/xml"/>
            </request>
          </method>
        </resource>
      </resources>
    </application>)
    , TestConfig(false, false, true, true, true, 1, true, true, true, "XalanC", true, true))

  // GET on /a requires foo:observer role
  it should behave like allowAccess(validator, "GET", "/a", List("foo:observer"))
  it should behave like allowAccess(validator, "GET", "/a", List("foo:observer", "foo:bar"))
  it should behave like preventAccess(validator, "GET", "/a", List("foo:bar"))
  it should behave like preventAccess(validator, "GET", "/a", List("foo:bar", "foo:creator"))
  it should behave like preventAccess(validator, "GET", "/a", List("foo:creator"))
  it should behave like preventAccessWhenNoXRoles(validator, "GET", "/a")

  // POST on /a requires foo:creator role
  it should behave like allowAccess(validator, "POST", "/a", List("foo:creator"))
  it should behave like allowAccess(validator, "POST", "/a", List("foo:bar", "foo:creator"))
  it should behave like preventAccess(validator, "POST", "/a", List("foo:bar"))
  it should behave like preventAccess(validator, "POST", "/a", List("foo:bar", "foo:observer"))
  it should behave like preventAccess(validator, "POST", "/a", List("foo:observer"))
  it should behave like preventAccessWhenNoXRoles(validator, "POST", "/a")

  // PUT with no role, should allow all access
  it should behave like allowAccess(validator, "PUT", "/a", List())
  it should behave like allowAccess(validator, "PUT", "/a", List("foo:bar"))
  it should behave like allowAccess(validator, "PUT", "/a", List("foo:observer", "foo:bar"))
  it should behave like allowAccess(validator, "PUT", "/a", List("foo:bar", "foo:jawsome"))
  it should behave like allowAccessWhenNoXRoles(validator, "PUT", "/a")

  // DELETE has multiple roles, treated as ORs, not ANDs
  it should behave like allowAccess(validator, "DELETE", "/a", List("foo:observer", "foo:bar"))
  it should behave like allowAccess(validator, "DELETE", "/a", List("foo:creator", "foo:bar"))
  it should behave like allowAccess(validator, "DELETE", "/a", List("foo:bar", "foo:creator"))
  it should behave like allowAccess(validator, "DELETE", "/a", List("foo:observer", "foo:creator"))
  it should behave like preventAccess(validator, "DELETE", "/a", List())
  it should behave like preventAccess(validator, "DELETE", "/a", List("foo:bar"))
  it should behave like preventAccess(validator, "DELETE", "/a", List("foo:bar", "foo:jawsome"))
  it should behave like preventAccess(validator, "DELETE", "/a", List("observer", "creator"))
  it should behave like preventAccessWhenNoXRoles(validator, "DELETE", "/a")
}

@RunWith(classOf[JUnitRunner])
class GivenAWadlWithRolesAtResourceLevel extends FlatSpec with RaxRolesBehaviors {
  val localWADLURI = (new File(System.getProperty("user.dir"), "mywadl.wadl")).toURI.toString

  val validator = Validator((localWADLURI,
    <application xmlns="http://wadl.dev.java.net/2009/02"
                 xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                 xmlns:tst="http://www.rackspace.com/repose/wadl/checker/step/test"
                 xmlns:rax="http://docs.rackspace.com/api">
      <resources base="https://test.api.openstack.com">
        <resource path="/a" rax:roles="foo:creator">
        <method name="POST">
          <request>
            <representation mediaType="application/xml"/>
          </request>
        </method>
        <method name="GET">
          <request>
            <representation mediaType="application/xml"/>
          </request>
        </method>
        <method name="PUT" rax:roles="foo:observer">
          <request>
            <representation mediaType="application/xml"/>
          </request>
        </method>
        <method name="DELETE" rax:roles="foo:observer foo:creator foo:admin">
          <request>
            <representation mediaType="application/xml"/>
          </request>
        </method>
          <resource path="/b" rax:roles="b:creator">
            <method name="POST">
              <request>
                <representation mediaType="application/xml"/>
              </request>
            </method>
            <method name="GET">
              <request>
                <representation mediaType="application/xml"/>
              </request>
            </method>
            <method name="PUT" rax:roles="b:observer">
              <request>
                <representation mediaType="application/xml"/>
              </request>
            </method>
            <method name="DELETE" rax:roles="b:observer b:creator b:admin">
              <request>
                <representation mediaType="application/xml"/>
              </request>
            </method>
          </resource>
      </resource>
      </resources>
    </application>)
    , TestConfig(false, false, true, true, true, 1, true, true, true, "XalanC", true, true))

  // When a single value rax:roles at resource level but not at method level
  it should behave like allowAccess(validator, "GET", "/a", List("foo:creator"))
  it should behave like preventAccess(validator, "GET", "/a", List("foo:observer"))
  it should behave like preventAccess(validator, "GET", "/a", List("b:observer"))
  it should behave like preventAccess(validator, "GET", "/a", List("b:creator"))
  it should behave like preventAccessWhenNoXRoles(validator, "GET", "/a")

  it should behave like allowAccess(validator, "POST", "/a", List("foo:creator"))
  it should behave like preventAccess(validator, "POST", "/a", List("foo:observer"))
  it should behave like preventAccessWhenNoXRoles(validator, "POST", "/a")

  // PUT has resource level foo:creator, method level foo:observer
  it should behave like allowAccess(validator, "PUT", "/a", List("foo:creator"))
  it should behave like allowAccess(validator, "PUT", "/a", List("foo:observer"))
  it should behave like allowAccess(validator, "PUT", "/a", List("foo:observer", "foo:creator"))

  it should behave like preventAccess(validator, "PUT", "/a", List("foo:bar"))
  it should behave like preventAccess(validator, "PUT", "/a", List())
  it should behave like preventAccess(validator, "PUT", "/a", List("foo:observe"))
  it should behave like preventAccessWhenNoXRoles(validator, "PUT", "/a")

  // DELETE has resource level foo:creator, method level foo:observer and foo:creator
  it should behave like allowAccess(validator, "DELETE", "/a", List("foo:creator"))
  it should behave like allowAccess(validator, "DELETE", "/a", List("foo:observer"))
  it should behave like allowAccess(validator, "DELETE", "/a", List("foo:observer", "foo:creator"))
  it should behave like allowAccess(validator, "DELETE", "/a", List("foo:admin"))

  it should behave like preventAccess(validator, "DELETE", "/a", List("foo:bar"))
  it should behave like preventAccess(validator, "DELETE", "/a", List())
  it should behave like preventAccess(validator, "DELETE", "/a", List("foo:observe"))
  it should behave like preventAccessWhenNoXRoles(validator, "DELETE", "/a")

  // Sub-resource b: inherits resource a foo:creator access
  // When a single value rax:roles at resource level but not at method level
  it should behave like allowAccess(validator, "GET", "/a/b", List("foo:creator"))
  it should behave like allowAccess(validator, "GET", "/a/b", List("b:creator"))
  it should behave like preventAccess(validator, "GET", "/a/b", List("foo:observer"))
  it should behave like preventAccessWhenNoXRoles(validator, "GET", "/a/b")

  it should behave like allowAccess(validator, "POST", "/a/b", List("foo:creator"))
  it should behave like allowAccess(validator, "POST", "/a/b", List("b:creator"))
  it should behave like preventAccess(validator, "POST", "/a/b", List("foo:observer"))
  it should behave like preventAccessWhenNoXRoles(validator, "POST", "/a/b")

  // PUT has resource level b:creator, method level b:observer, parent resource foo:creator
  it should behave like allowAccess(validator, "PUT", "/a/b", List("foo:creator"))
  it should behave like allowAccess(validator, "PUT", "/a/b", List("b:observer"))
  it should behave like allowAccess(validator, "PUT", "/a/b", List("b:observer", "foo:creator"))

  it should behave like preventAccess(validator, "PUT", "/a/b", List("foo:observer"))
  it should behave like preventAccess(validator, "PUT", "/a/b", List("foo:bar"))
  it should behave like preventAccess(validator, "PUT", "/a/b", List())
  it should behave like preventAccess(validator, "PUT", "/a/b", List("b:observe"))
  it should behave like preventAccess(validator, "PUT", "/a/b", List("observer"))
  it should behave like preventAccessWhenNoXRoles(validator, "PUT", "/a/b")

  // DELETE has parent resource level foo:creator, method level b:observer and b:creator and b:admin
  it should behave like allowAccess(validator, "DELETE", "/a/b", List("foo:creator"))
  it should behave like allowAccess(validator, "DELETE", "/a/b", List("b:observer"))
  it should behave like allowAccess(validator, "DELETE", "/a/b", List("b:observer", "foo:creator"))
  it should behave like allowAccess(validator, "DELETE", "/a/b", List("b:admin"))

  it should behave like preventAccess(validator, "DELETE", "/a/b", List("foo:bar"))
  it should behave like preventAccess(validator, "DELETE", "/a/b", List())
  it should behave like preventAccess(validator, "DELETE", "/a/b", List("foo:observe"))
  it should behave like preventAccess(validator, "DELETE", "/a/b", List("foo:observer"))
  it should behave like preventAccess(validator, "DELETE", "/a/b", List("foo:admin"))

  it should behave like preventAccessWhenNoXRoles(validator, "DELETE", "/a/b")
}


@RunWith(classOf[JUnitRunner])
class GivenAWadlWithRolesOfAll extends FlatSpec with RaxRolesBehaviors {

  val localWADLURI = (new File(System.getProperty("user.dir"), "mywadl.wadl")).toURI.toString
  val validator = Validator((localWADLURI,
    <application xmlns="http://wadl.dev.java.net/2009/02"
                 xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                 xmlns:tst="http://www.rackspace.com/repose/wadl/checker/step/test"
                 xmlns:rax="http://docs.rackspace.com/api">
      <resources base="https://test.api.openstack.com">
        <resource path="/a" rax:roles="a:admin">
          <method name="POST" rax:roles="foo:creator">
            <request>
              <representation mediaType="application/xml"/>
            </request>
          </method>
          <method name="GET" rax:roles="#all">
            <request>
              <representation mediaType="application/xml"/>
            </request>
          </method>
          <method name="PUT">
            <request>
              <representation mediaType="application/xml"/>
            </request>
          </method>
        </resource>
        <resource path="/b" rax:roles="#all">
          <resource path="/c" rax:roles="c:admin">
            <method name="POST">
              <request>
                <representation mediaType="application/xml"/>
              </request>
            </method>
            <method name="GET">
              <request>
                <representation mediaType="application/xml"/>
              </request>
            </method>
          </resource>
        </resource>
      </resources>
    </application>)
    , TestConfig(false, false, true, true, true, 1, true, true, true, "XalanC", true, true))

  // GET on /a allows all access
  it should behave like allowAccess(validator, "GET", "/a", List("foo:observer"))
  it should behave like allowAccess(validator, "GET", "/a", List("foo:observer", "foo:bar"))
  it should behave like allowAccess(validator, "GET", "/a", List("foo:bar"))
  it should behave like allowAccess(validator, "GET", "/a", List("foo:bar", "foo:creator"))
  it should behave like allowAccess(validator, "GET", "/a", List("foo:creator"))
  it should behave like allowAccessWhenNoXRoles(validator, "GET", "/a")



}
