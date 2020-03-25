package com.IEEEpaper.examples;

import java.text.DecimalFormat;
import java.util.*;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.ex.DatacenterBrokerEX;
import org.cloudbus.cloudsim.examples.CloudSimExample1;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

/**
 * A simple example showing how to submit and destroy VMs and cloudlets with a delay.
 * Based on {@link CloudSimExample1}.
 */
public class DelayExample1 {

    public static void main(String[] args) throws Exception {

        // Step1: Initialize the CloudSim package. It should be called
        // before creating any entities.
        int numBrokers = 1; // number of brokers we'll be using

        // Initialize CloudSim
        CloudSim.init(numBrokers, Calendar.getInstance(), false);

        // Step 2: Create Datacenter
        // Datacenters are the resource providers in CloudSim. We need at
        // list one of them to run a CloudSim simulation
        @SuppressWarnings("unused")
        Datacenter datacenter0 = createDatacenter("Datacenter_0");

        // Step 3: Create Broker
        DatacenterBrokerEX broker = new DatacenterBrokerEX("Broker", -1);

        // Step 4: Create one virtual machine
        int vmid = 0;
        int mips = 1000;
        long size = 10000; // image size (MB)
        int ram = 512; // vm memory (MB)
        long bw = 1000;
        int pesNumber = 1; // number of cpus
        String vmm = "Xen"; // VMM name
        Vm vm = new Vm(vmid, broker.getId(), mips, pesNumber, ram, bw, size, vmm, new CloudletSchedulerTimeShared());
        Vm vm2 = new Vm(++vmid, broker.getId(), mips, pesNumber, ram, bw, size, vmm, new CloudletSchedulerTimeShared());

        List<Vm> vms = new ArrayList<Vm>();
        vms.add(vm);
        vms.add(vm2);
        // submit vm to the broker after 2 seconds
        broker.submitVmList(vms);
//        broker.createVmsAfter(vms, 2);
//        broker.createVmsAfter(Collections.singletonList(vm), 2);
//        broker.createVmsAfter(Collections.singletonList(vm2), 51);
        // destroy VM after 500 seconds
//        broker.destroyVMsAfter(Collections.singletonList(vm), 8000);

        // Step 5: Create a Cloudlet
        // Cloudlet properties
        int id = 0;
        long length = 400000;
        long fileSize = 300;
        long outputSize = 300;
        UtilizationModel utilizationModel = new UtilizationModelFull();

        Cloudlet cloudlet = new Cloudlet(id, length, pesNumber, fileSize, outputSize, utilizationModel,
                new UtilizationModelFull(), new UtilizationModelFull());
        cloudlet.setUserId(broker.getId());

        cloudlet.setVmId(vm.getId());



        Cloudlet cloudlet2 = new Cloudlet(++id, length*2, pesNumber, fileSize, outputSize, new UtilizationModelFull(),
                new UtilizationModelFull(), new UtilizationModelFull());
        cloudlet2.setUserId(broker.getId());
        cloudlet2.setVmId(vm2.getId());


//        broker.submitCloudletList(cloudlets, 10);
        broker.submitCloudletList(Collections.singletonList(cloudlet), 10);
        broker.submitCloudletList(Collections.singletonList(cloudlet2), 50);

        // Sixth step: Starts the simulation
        CloudSim.startSimulation();
        // ...
        CloudSim.stopSimulation();

        //Final step: Print results when simulation is over
        List<Cloudlet> newList = broker.getCloudletReceivedList();
        printCloudletList(newList);
    }

    /**
     * Creates the datacenter.
     *
     * @param name
     *            the name
     *
     * @return the datacenter
     * @throws Exception
     */
    private static Datacenter createDatacenter(String name) throws Exception {

        // Here are the steps needed to create a PowerDatacenter:
        // 1. We need to create a list to store
        // our machine
        List<Host> hostList = new ArrayList<Host>();

        // 2. A Machine contains one or more PEs or CPUs/Cores.
        // In this example, it will have only one core.
        List<Pe> peList = new ArrayList<Pe>();

        int mips = 5000;

        // 3. Create PEs and add these into a list.
        peList.add(new Pe(0, new PeProvisionerSimple(mips))); // need to store
        // Pe id and MIPS
        // Rating

        // 4. Create Host with its id and list of PEs and add them to the list
        // of machines
        int hostId = 0;
        int ram = 2048; // host memory (MB)
        long storage = 1000000; // host storage
        int bw = 10000;

        hostList.add(
                new Host(
                        hostId,
                        new RamProvisionerSimple(ram),
                        new BwProvisionerSimple(bw),
                        storage,
                        peList,
                        new VmSchedulerTimeShared(peList)
                )
        ); // This is our machine

        // 5. Create a DatacenterCharacteristics object that stores the
        // properties of a data center: architecture, OS, list of
        // Machines, allocation policy: time- or space-shared, time zone
        // and its price (G$/Pe time unit).
        String arch = "x86"; // system architecture
        String os = "Linux"; // operating system
        String vmm = "Xen";
        double time_zone = 10.0; // time zone this resource located
        double cost = 3.0; // the cost of using processing in this resource
        double costPerMem = 0.05; // the cost of using memory in this resource
        double costPerStorage = 0.001; // the cost of using storage in this
        // resource
        double costPerBw = 0.0; // the cost of using bw in this resource
        LinkedList<Storage> storageList = new LinkedList<Storage>(); // we are
        // not
        // adding
        // SAN
        // devices by now

        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
                arch, os, vmm, hostList, time_zone, cost, costPerMem,
                costPerStorage, costPerBw);

        // 6. Finally, we need to create a PowerDatacenter object.
        return new Datacenter(name, characteristics, new VmAllocationPolicySimple(hostList), storageList, 0);
    }

    /**
     * Prints the Cloudlet objects.
     *
     * @param list list of Cloudlets
     */
    static void printCloudletList(List<Cloudlet> list) {
        int size = list.size();
        Cloudlet cloudlet;

        String indent = "    ";
        Log.printLine();
        Log.printLine("========== OUTPUT ==========");
        Log.printLine("Cloudlet ID" + indent + "STATUS" + indent
                + "Data center ID" + indent + "VM ID" + indent + "Time" + indent
                + "Start Time" + indent + "Finish Time");

        DecimalFormat dft = new DecimalFormat("###.##");
        for (Cloudlet aList : list) {
            cloudlet = aList;
            Log.print(indent + cloudlet.getCloudletId() + indent + indent);

            if (cloudlet.getStatus() == Cloudlet.SUCCESS) {
                Log.print("SUCCESS");

                Log.printLine(indent + indent + cloudlet.getResourceId()
                        + indent + indent + indent + cloudlet.getVmId()
                        + indent + indent
                        + dft.format(cloudlet.getActualCPUTime()) + indent
                        + indent + dft.format(cloudlet.getExecStartTime())
                        + indent + indent
                        + dft.format(cloudlet.getFinishTime()));
            }
        }
    }

}
