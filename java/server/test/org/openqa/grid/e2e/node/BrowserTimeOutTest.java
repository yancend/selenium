/*
Copyright 2011 Selenium committers
Copyright 2011 Software Freedom Conservancy

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package org.openqa.grid.e2e.node;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.grid.common.GridRole;
import org.openqa.grid.e2e.utils.GridTestHelper;
import org.openqa.grid.e2e.utils.RegistryTestHelper;
import org.openqa.grid.internal.utils.GridHubConfiguration;
import org.openqa.grid.internal.utils.SelfRegisteringRemote;
import org.openqa.grid.web.Hub;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.net.PortProber;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

/**
 * checks that the session is stopped and browser released when browser timeout happens.
 */
public class BrowserTimeOutTest {

  private static Hub hub;
  private static SelfRegisteringRemote node;

  @BeforeClass
  public static void setup() throws Exception {
    GridHubConfiguration gridHubConfiguration = new GridHubConfiguration();
    gridHubConfiguration.setPort(PortProber.findFreePort());
    gridHubConfiguration.setHost("localhost");

    gridHubConfiguration.setBrowserTimeout(10000);
    gridHubConfiguration.setServlets(Arrays.asList("org.openqa.grid.e2e.node.SlowServlet"));
    hub = GridTestHelper.getHub(gridHubConfiguration);

    // register a selenium 1
    node = GridTestHelper.getRemoteWithoutCapabilities(hub.getUrl(), GridRole.NODE);
    node.addBrowser(GridTestHelper.getSelenium1FirefoxCapability(), 1);
    node.addBrowser(GridTestHelper.getDefaultBrowserCapability(), 1);
    node.startRemoteServer();
    node.sendRegistrationRequest();

    RegistryTestHelper.waitForNode(hub.getRegistry(), 1);
  }


  @Test
  public void testWebDriverTimesOut() throws InterruptedException, MalformedURLException {
    String url = "http://" + hub.getHost() + ":" + hub.getPort() + "/grid/admin/SlowServlet";
    DesiredCapabilities caps = GridTestHelper.getDefaultBrowserCapability();
    WebDriver driver = new RemoteWebDriver(new URL(hub.getUrl() + "/wd/hub"), caps);

    try {
      driver.get(url);
    } catch(WebDriverException ignore){
    } finally {
      RegistryTestHelper.waitForActiveTestSessionCount(hub.getRegistry(), 0);
    }
  }

  @AfterClass
  public static void teardown() throws Exception {
    node.stopRemoteServer();
    hub.stop();
  }

}
