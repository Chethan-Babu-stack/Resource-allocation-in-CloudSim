package com.IEEEpaper.examples;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.core.predicates.PredicateType;
import org.cloudbus.cloudsim.power.PowerDatacenterNonPowerAware;
import org.cloudbus.cloudsim.power.PowerHost;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

public class MyDatacenter extends PowerDatacenterNonPowerAware {

    /**
     * Instantiates a new datacenter.
     *
     * @param name               the datacenter name
     * @param characteristics    the datacenter characteristics
     * @param vmAllocationPolicy the vm provisioner
     * @param storageList        the storage list
     * @param schedulingInterval the scheduling interval
     * @throws Exception the exception
     */
    public MyDatacenter(String name, DatacenterCharacteristics characteristics, VmAllocationPolicy vmAllocationPolicy, List<Storage> storageList, double schedulingInterval) throws Exception {
        super(name, characteristics, vmAllocationPolicy, storageList, schedulingInterval);
    }

    @Override
    protected void updateCloudletProcessing() {

        if (getCloudletSubmitted() == -1 || getCloudletSubmitted() == CloudSim.clock()) {
            CloudSim.cancelAll(getId(), new PredicateType(CloudSimTags.VM_DATACENTER_EVENT));
            schedule(getId(), getSchedulingInterval(), CloudSimTags.VM_DATACENTER_EVENT);
            return;
        }
        double currentTime = CloudSim.clock();
        double timeframePower = 0.0;

        if (currentTime > getLastProcessTime()) {
            double timeDiff = currentTime - getLastProcessTime();
            double minTime = Double.MAX_VALUE;

//            Log.printLine("\n");

            for (PowerHost host : this.<PowerHost> getHostList()) {
//                Log.formatLine("%.2f: Host #%d", CloudSim.clock(), host.getId());
//
                double hostPower = 0.0;
//
                try {
                    hostPower = host.getMaxPower() * timeDiff;
                    timeframePower += hostPower;
                } catch (Exception e) {
                    e.printStackTrace();
                }
//
//                Log.formatLine(
//                        "%.2f: Host #%d utilization is %.2f%%",
//                        CloudSim.clock(),
//                        host.getId(),
//                        host.getUtilizationOfCpu() * 100);
//                Log.formatLine(
//                        "%.2f: Host #%d energy is %.2f W*sec",
//                        CloudSim.clock(),
//                        host.getId(),
//                        hostPower);
            }

//            Log.formatLine("\n%.2f: Consumed energy is %.2f W*sec\n", CloudSim.clock(), timeframePower);

//            Log.printLine("\n\n--------------------------------------------------------------\n\n");

            for (PowerHost host : this.<PowerHost> getHostList()) {
//                Log.formatLine("\n%.2f: Host #%d", CloudSim.clock(), host.getId());

                double time = host.updateVmsProcessing(currentTime); // inform VMs to update
                // processing
                if (time < minTime) {
                    minTime = time;
                }
            }

            setPower(getPower() + timeframePower);

            checkCloudletCompletion();

//            /** Remove completed VMs **/
//            for (PowerHost host : this.<PowerHost> getHostList()) {
//                for (Vm vm : host.getCompletedVms()) {
//                    getVmAllocationPolicy().deallocateHostForVm(vm);
//                    getVmList().remove(vm);
//                    Log.printLine("VM #" + vm.getId() + " has been deallocated from host #" + host.getId());
//                }
//            }

            Log.printLine();

            if (!isDisableMigrations()) {
                List<Map<String, Object>> migrationMap = getVmAllocationPolicy().optimizeAllocation(
                        getVmList());

                if (migrationMap != null) {
                    for (Map<String, Object> migrate : migrationMap) {
                        Vm vm = (Vm) migrate.get("vm");
                        PowerHost targetHost = (PowerHost) migrate.get("host");
                        PowerHost oldHost = (PowerHost) vm.getHost();

                        if (oldHost == null) {
                            Log.formatLine(
                                    "%.2f: Migration of VM #%d to Host #%d is started",
                                    CloudSim.clock(),
                                    vm.getId(),
                                    targetHost.getId());
                        } else {
                            Log.formatLine(
                                    "%.2f: Migration of VM #%d from Host #%d to Host #%d is started",
                                    CloudSim.clock(),
                                    vm.getId(),
                                    oldHost.getId(),
                                    targetHost.getId());
                        }

                        targetHost.addMigratingInVm(vm);
                        incrementMigrationCount();

                        /** VM migration delay = RAM / bandwidth + C (C = 10 sec) **/
                        send(
                                getId(),
                                vm.getRam() / ((double) vm.getBw() / 8000) + 10,
                                CloudSimTags.VM_MIGRATE,
                                migrate);
                    }
                }
            }

            // schedules an event to the next time
            if (minTime != Double.MAX_VALUE) {
                CloudSim.cancelAll(getId(), new PredicateType(CloudSimTags.VM_DATACENTER_EVENT));
                // CloudSim.cancelAll(getId(), CloudSim.SIM_ANY);
                send(getId(), getSchedulingInterval(), CloudSimTags.VM_DATACENTER_EVENT);
            }

            setLastProcessTime(currentTime);
        }

        computeParams();
    }


    private void computeParams() {

        double clock = CloudSim.clock();

        double avgUtil = 0.0;
        double avgRam = 0.0;
        double avgBw = 0.0;
        int freePes=0;

        int freeHostCount = 0;

        for (PowerHost host: this.<PowerHost> getHostList()) {
            avgUtil += host.getUtilizationOfCpu() * 100;
         //   avgRam += (host.getUtilizationOfRam()/host.getRam()) * 100;
         //   avgBw += (host.getUtilizationOfBw()/host.getBw()) * 100;
        //    freePes += host.getNumberOfFreePes();

            int busyVmCount = 0;

            for (Vm vm : host.getVmList()) {
                if (vm.getCloudletScheduler().runningCloudlets() > 0)
                    busyVmCount++;
            }

            if (busyVmCount == 0)
                freeHostCount++;
        }
        //Log.printLine(avgUtil);
        int busyHostCount = getHostList().size() - freeHostCount;

        avgBw = (busyHostCount != 0) ? avgBw / busyHostCount : 0.0d;
        avgRam = (busyHostCount != 0) ? avgRam / busyHostCount : 0.0d;
        avgUtil = (busyHostCount != 0) ? avgUtil / busyHostCount : 0.0d;


       String data = (clock + Util.DELIM) +
           //     busyHostCount + Util.DELIM +
                Util.DFT.format(avgUtil) + Util.DELIM + "     "
           //     Util.DFT.format(avgRam) + Util.DELIM +
           //     Util.DFT.format(avgBw) + Util.DELIM +
             //   Util.DFT.format(freePes)+"     "
                ;


            Util.writeRowToLog(data);
            
    }
}

