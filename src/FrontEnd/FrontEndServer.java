package FrontEnd;

import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import org.omg.PortableServer.Servant;

import FrontEndAPP.FrontEnd;
import FrontEndAPP.FrontEndHelper;
import Model.FEPort;

public class FrontEndServer {

	public static void main(String args[]) {
		try{
			// create and initialize the ORB //// get reference to rootpoa &amp; activate the POAManager
			ORB orb = ORB.init(args, null);
			POA rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
			rootpoa.the_POAManager().activate();
			// create servant and register it with the ORB
			FrontEndObj libobj = new FrontEndObj(FEPort.FE_PORT.FEPort);
			libobj.setORB(orb);

			// get object reference from the servant
			org.omg.CORBA.Object ref = rootpoa.servant_to_reference(libobj);

			FrontEnd href = FrontEndHelper.narrow(ref);

			org.omg.CORBA.Object objRef =  orb.resolve_initial_references("NameService");
			NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

			NameComponent path[] = ncRef.to_name( "FE" );
			ncRef.rebind(path, href);

			System.out.println("FE Server ready and waiting ...");

			// wait for invocations from clients
			for (;;){
				orb.run();
			}
		}

		catch (Exception e) {
			System.err.println("ERROR: " + e);
			e.printStackTrace(System.out);
		}

		System.out.println("HelloServer Exiting ...");

	}
}
