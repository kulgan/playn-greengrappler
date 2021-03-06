using System;
using MonoTouch.Foundation;
using MonoTouch.UIKit;

using playn.ios;
using playn.core;
using se.darkbits.greengrappler;

namespace com.meros.playn
{
  [Register ("AppDelegate")]
  public partial class AppDelegate : UIApplicationDelegate {
    public override bool FinishedLaunching (UIApplication app, NSDictionary options) {
      app.SetStatusBarHidden(true, true);
      var pf = IOSPlatform.register(app, IOSPlatform.SupportedOrients.LANDSCAPES);
      pf.assets().setPathPrefix("assets");
      PlayN.run(
		new GreenGrappler(
				true));
      return true;
    }
  }

  public class Application {
    static void Main (string[] args) {
      UIApplication.Main (args, null, "AppDelegate");
    }
  }
}
