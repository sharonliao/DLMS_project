package FrontEnd;

import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

import FrontEndAPP.FrontEndHelper;

public class FrontEnd {

	public static void main(String[] args) {
		try {
			// create and initialize the ORB 
			ORB orb = ORB.init(args, null);
			
			// get reference to rootpoa; 
			POA rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
			//activate the POAManager
			rootpoa.the_POAManager().activate();

			// create servant and register it with the ORB,create the reflection of servant and object reference;
			FrontEndImp frontend = new FrontEndImp();
			frontend.setORB(orb);
			
			// get object reference from the servant
			org.omg.CORBA.Object ref = rootpoa.servant_to_reference(frontend);
			FrontEndAPP.FrontEnd href = FrontEndHelper.narrow(ref);
			
			org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
			NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
			
			NameComponent path[] = ncRef.to_name("FrontEnd");
			ncRef.rebind(path, href);

			System.out.println("FrontEnd ready and waiting ...");

			// wait for invocations from clients
			for (;;) {
				orb.run();
			}
		}

		catch (Exception e) {
			System.err.println("ERROR: " + e);
			e.printStackTrace(System.out);
		}
		System.out.println("FrontEnd Server Exiting ...");
	}

}
