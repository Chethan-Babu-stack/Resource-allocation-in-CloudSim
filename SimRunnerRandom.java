package com.IEEEpaper.examples;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletSchedulerDynamicWorkload;
import org.cloudbus.cloudsim.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.ex.DatacenterBrokerEX;
import org.cloudbus.cloudsim.power.PowerDatacenterNonPowerAware;
import org.cloudbus.cloudsim.power.PowerHost;
import org.cloudbus.cloudsim.power.PowerVm;
import org.cloudbus.cloudsim.power.PowerVmAllocationPolicySimple;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;


public class SimRunnerRandom {

    /**
     * The main method.
     *
     * @param args the arguments
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static void main(String[] args) throws IOException {

        // Create a folder for the sequence of simulations
        String simName = new SimpleDateFormat("yyyyMMdd'_'HHmmss").format(new Date());

        File outputFolder = new File("output/" + simName);
        if (!outputFolder.mkdir()) {
            System.err.println("Folder could not be created: " + outputFolder.getAbsolutePath());
            System.exit(1);
        }


        int[] hosts = {100};
        int[] cloudlets = {500};
        

        for (int hostCount : hosts) {
            for (int cloudletCount : cloudlets) {
                runSim(outputFolder.getAbsolutePath(), hostCount, cloudletCount);
            }
        }
    }

    private static void runSim(String outputPrefix, int hostCount, int cloudletCount) throws FileNotFoundException {
        String expName = hostCount + "_" + cloudletCount;

        String outFolderPath = outputPrefix + "/" + expName;

        Util.init(outFolderPath, expName);
        Log.setOutput(new FileOutputStream(Util.outFileOut));


        Log.printLine("Starting " + expName);

        try {
            CloudSim.init(1, Calendar.getInstance(), false);

            DatacenterBrokerEX broker = Helper.createBroker();
            int brokerId = broker.getId();

            List<Cloudlet> cloudletList = Helper.createCloudletList(brokerId, cloudletCount);
            int vms = 150;/// number of vms
            List<Vm> vmList = Helper.createVmList(brokerId, vms);
            List<PowerHost> hostList = Helper.createHostList(hostCount);

            PowerDatacenterNonPowerAware datacenter = (PowerDatacenterNonPowerAware) Helper.createDatacenter(
                    "Datacenter",
                    MyDatacenter.class,
                    hostList,
                    new PowerVmAllocationPolicySimple(hostList));

         //   datacenter.setDisableMigrations(true);
            /////////////////////////////////////////////////////////////////////////////////////
            
            
            // adding vms dynamically
            
            for(int i=150 ; i<250 ; i++){
            	
        //    	Random ran1 = new Random();
            Vm vm = new PowerVm(
                    i,
                    brokerId,
                    Constants.VM_MIPS[0],
                    Constants.VM_PES[0],
                    Constants.VM_RAM[0],
                    Constants.VM_BW,
                    Constants.VM_SIZE,
                    1,
                    "Xen",
                    new CloudletSchedulerDynamicWorkload(Constants.VM_MIPS[0], Constants.VM_PES[0]),
                    Constants.SCHEDULING_INTERVAL);

            // submit vm to the broker after i+20 seconds 
            broker.createVmsAfter(Collections.singletonList(vm), i + 20);  
            
            }
            ///////////////////////////////////////////////////////////////////////////////////////
            broker.submitVmList(vmList);
//            broker.submitCloudletList(cloudletList);
            submitCloudletsRandomly(broker, cloudletList);


            String delim = ", ";
            String data = hostList.size() + delim + cloudletList.size();

            CloudSim.startSimulation();


            CloudSim.stopSimulation();

            List<Cloudlet> newList = broker.getCloudletReceivedList();
            Log.printLine("Received " + newList.size() + " cloudlets");

            // Recieved Cloudlets Count
            data += delim + newList.size();
            Util.writeRowToDat(data);

            // Cloudlet Execution time
            for (Cloudlet c : newList) {
                Util.writeRowToDat(c.getCloudletId() + delim    // Cloudlet Id
                        + c.getVmId() + delim         // vm id
                        + c.getCloudletLength() + delim
                        + c.getActualCPUTime() + delim          // Actual CPU Time
                        + c.getExecStartTime() + delim          // Start Time
                        + c.getFinishTime());                   // Finish Time
            }

            DelayExample1.printCloudletList(newList);

        } catch (Exception e) {
            e.printStackTrace();
            Log.printLine("The simulation has been terminated due to an unexpected error");
            System.exit(0);
        }

        Log.printLine("Finished " + expName);

    }


    private static void submitCloudletsRandomly(DatacenterBrokerEX broker, List<Cloudlet> cloudletList) {

        Random random = new Random(Constants.CLOUDLET_DELAY_SEED);

        for (Cloudlet cloudlet : cloudletList) {

            double delay = (1 + random.nextInt(Constants.CLOUDLET_DELAY_BOUND)) * 10;

            broker.submitCloudletList(Collections.singletonList(cloudlet), delay);
        }
    }
}
