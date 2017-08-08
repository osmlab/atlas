package org.openstreetmap.atlas.utilities.runtime.system;

import java.io.Serializable;
import java.lang.management.ClassLoadingMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.openstreetmap.atlas.utilities.runtime.system.memory.Memory;
import org.openstreetmap.atlas.utilities.scalars.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author cstaylor
 * @author matthieun
 */
public final class SystemInfo
{
    /**
     * @author cstaylor
     */
    public static class SystemInfoBean implements Serializable
    {
        /**
         * @author cstaylor
         */
        public static class MemoryPoolBean
        {
            private final String name;
            private final String managerNames;
            private final Memory currentInitialized;
            private final Memory currentUsed;
            private final Memory currentCommitted;
            private final Memory currentMaximum;
            private final Memory peakInitialized;
            private final Memory peakUsed;
            private final Memory peakCommitted;
            private final Memory peakMaximum;
            private Memory collectionInitialized;
            private Memory collectionUsed;
            private Memory collectionCommitted;
            private Memory collectionMaximum;
            private final boolean collection;

            public MemoryPoolBean(final MemoryPoolMXBean pool)
            {
                final MemoryUsage currentUsage = pool.getUsage();
                final MemoryUsage peakUsage = pool.getPeakUsage();
                final MemoryUsage collectionUsage = pool.getCollectionUsage();
                this.collection = collectionUsage != null;
                this.name = pool.getName();
                this.managerNames = Arrays.toString(pool.getMemoryManagerNames());

                this.currentInitialized = Memory.bytes(currentUsage.getInit());
                this.currentUsed = Memory.bytes(currentUsage.getUsed());
                this.currentCommitted = Memory.bytes(currentUsage.getCommitted());
                this.currentMaximum = Memory.bytes(currentUsage.getMax());

                this.peakInitialized = Memory.bytes(peakUsage.getInit());
                this.peakUsed = Memory.bytes(peakUsage.getUsed());
                this.peakCommitted = Memory.bytes(peakUsage.getCommitted());
                this.peakMaximum = Memory.bytes(peakUsage.getMax());

                if (this.collection)
                {
                    this.collectionInitialized = Memory.bytes(collectionUsage.getInit());
                    this.collectionUsed = Memory.bytes(collectionUsage.getUsed());
                    this.collectionCommitted = Memory.bytes(collectionUsage.getCommitted());
                    this.collectionMaximum = Memory.bytes(collectionUsage.getMax());
                }
            }

            public String getCollectionCommitted()
            {
                return this.collectionCommitted.toString();
            }

            public String getCollectionInitialized()
            {
                return this.collectionInitialized.toString();
            }

            public String getCollectionMaximum()
            {
                return this.collectionMaximum.toString();
            }

            public String getCollectionUsed()
            {
                return this.collectionUsed.toString();
            }

            public String getCurrentCommitted()
            {
                return this.currentCommitted.toString();
            }

            public String getCurrentInitialized()
            {
                return this.currentInitialized.toString();
            }

            public String getCurrentMaximum()
            {
                return this.currentMaximum.toString();
            }

            public String getCurrentUsed()
            {
                return this.currentUsed.toString();
            }

            public String getManagerNames()
            {
                return this.managerNames;
            }

            public String getName()
            {
                return this.name;
            }

            public String getPeakCommitted()
            {
                return this.peakCommitted.toString();
            }

            public String getPeakInitialized()
            {
                return this.peakInitialized.toString();
            }

            public String getPeakMaximum()
            {
                return this.peakMaximum.toString();
            }

            public String getPeakUsed()
            {
                return this.peakUsed.toString();
            }

            public boolean isCollection()
            {
                return this.collection;
            }

            @Override
            public String toString()
            {
                return toString("");
            }

            public String toString(final String header)
            {
                final StringBuilder builder = new StringBuilder();
                builder.append(header);
                builder.append("Name: ");
                builder.append(this.name);
                builder.append(System.lineSeparator());
                builder.append(header);
                builder.append("Manager Names: ");
                builder.append(this.managerNames);
                builder.append(System.lineSeparator());
                builder.append(header);
                builder.append("Current Init: ");
                builder.append(this.currentInitialized);
                builder.append(System.lineSeparator());
                builder.append(header);
                builder.append("Current USed: ");
                builder.append(this.currentUsed);
                builder.append(System.lineSeparator());
                builder.append(header);
                builder.append("Current Committed: ");
                builder.append(this.currentCommitted);
                builder.append(System.lineSeparator());
                builder.append(header);
                builder.append("Current Maximum: ");
                builder.append(this.currentMaximum);
                builder.append(System.lineSeparator());
                builder.append(header);
                builder.append("Peak Init: ");
                builder.append(this.peakInitialized);
                builder.append(System.lineSeparator());
                builder.append(header);
                builder.append("Peak USed: ");
                builder.append(this.peakUsed);
                builder.append(System.lineSeparator());
                builder.append(header);
                builder.append("Peak Committed: ");
                builder.append(this.peakCommitted);
                builder.append(System.lineSeparator());
                builder.append(header);
                builder.append("Peak Maximum: ");
                builder.append(this.peakMaximum);
                if (this.collection)
                {
                    builder.append(System.lineSeparator());
                    builder.append(header);
                    builder.append("Collection Initialized: ");
                    builder.append(this.collectionInitialized);
                    builder.append(System.lineSeparator());
                    builder.append(header);
                    builder.append("Collection USed: ");
                    builder.append(this.collectionUsed);
                    builder.append(System.lineSeparator());
                    builder.append(header);
                    builder.append("Collection Committed: ");
                    builder.append(this.collectionCommitted);
                    builder.append(System.lineSeparator());
                    builder.append(header);
                    builder.append("Collection Maximum: ");
                    builder.append(this.collectionMaximum);
                }
                return builder.toString();
            }

        }

        private static final long serialVersionUID = 8527234032101389715L;

        private String vmName;
        private String vmVendor;
        private String vmVersion;
        private Date startTime;
        private Duration upTime;
        private List<String> vmArgs;
        private int currentlyLoadedClasses;
        private long totalLoadedClasses;
        private long totalUnloadedClasses;
        private String nativeArchitecture;
        private int cpus;
        private String osName;
        private String osVersion;
        private Memory heapInitialized;
        private Memory heapCommitted;
        private Memory heapUsed;
        private Memory heapMaximum;
        private Memory nonHeapInitialized;
        private Memory nonHeapCommitted;
        private Memory nonHeapUsed;
        private Memory nonHeapMaximum;

        private final List<MemoryPoolMXBean> memoryPools = new ArrayList<>();
        private String vmSpecVersion;

        public void add(final Collection<MemoryPoolMXBean> beans)
        {
            this.memoryPools.addAll(beans);
        }

        public void add(final MemoryPoolMXBean currentBean)
        {
            this.memoryPools.add(currentBean);
        }

        public int getCpus()
        {
            return this.cpus;
        }

        public String getCurrentlyLoadedClasses()
        {
            return DecimalFormat.getIntegerInstance().format(this.currentlyLoadedClasses);
        }

        public String getHeapCommitted()
        {
            return this.heapCommitted.toString();
        }

        public String getHeapInitialized()
        {
            return this.heapInitialized.toString();
        }

        public String getHeapMaximum()
        {
            return this.heapMaximum.toString();
        }

        public String getHeapUsed()
        {
            return this.heapUsed.toString();
        }

        public List<MemoryPoolBean> getMemoryPools()
        {
            final ArrayList<MemoryPoolBean> result = new ArrayList<>();
            for (final MemoryPoolMXBean pool : this.memoryPools)
            {
                result.add(new MemoryPoolBean(pool));
            }
            return result;
        }

        public String getNativeArchitecture()
        {
            return this.nativeArchitecture;
        }

        public String getNonHeapCommitted()
        {
            return this.nonHeapCommitted.toString();
        }

        public String getNonHeapInitialized()
        {
            return this.nonHeapInitialized.toString();
        }

        public String getNonHeapMaximum()
        {
            return this.nonHeapMaximum.toString();
        }

        public String getNonHeapUsed()
        {
            return this.nonHeapUsed.toString();
        }

        public String getOsName()
        {
            return this.osName;
        }

        public String getOsVersion()
        {
            return this.osVersion;
        }

        public String getStartTime()
        {
            return this.startTime == null ? ""
                    : new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(this.startTime);
        }

        public String getTotalLoadedClasses()
        {
            return DecimalFormat.getIntegerInstance().format(this.totalLoadedClasses);
        }

        public String getTotalUnloadedClasses()
        {
            return DecimalFormat.getIntegerInstance().format(this.totalUnloadedClasses);
        }

        public String getUpTime()
        {
            return this.upTime == null ? "" : this.upTime.toString();
        }

        public List<String> getVmArgs()
        {
            return this.vmArgs;
        }

        public String getVmName()
        {
            return this.vmName;
        }

        public String getVmSpecVersion()
        {
            return this.vmSpecVersion;
        }

        public String getVmVendor()
        {
            return this.vmVendor;
        }

        public String getVmVersion()
        {
            return this.vmVersion;
        }

        public void setCpus(final int cpus)
        {
            this.cpus = cpus;
        }

        public void setCurrentlyLoadedClasses(final int currentlyLoadedClasses)
        {
            this.currentlyLoadedClasses = currentlyLoadedClasses;
        }

        public void setHeapCommitted(final Memory heapCommitted)
        {
            this.heapCommitted = heapCommitted;
        }

        public void setHeapInitialized(final Memory heapInit)
        {
            this.heapInitialized = heapInit;
        }

        public void setHeapMaximum(final Memory heapMax)
        {
            this.heapMaximum = heapMax;
        }

        public void setHeapUsed(final Memory heapUsed)
        {
            this.heapUsed = heapUsed;
        }

        public void setNativeArchitecture(final String nativeArchitecture)
        {
            this.nativeArchitecture = nativeArchitecture;
        }

        public void setNonHeapCommitted(final Memory nonHeapCommitted)
        {
            this.nonHeapCommitted = nonHeapCommitted;
        }

        public void setNonHeapInitialized(final Memory nonHeapInit)
        {
            this.nonHeapInitialized = nonHeapInit;
        }

        public void setNonHeapMaximum(final Memory nonHeapMax)
        {
            this.nonHeapMaximum = nonHeapMax;
        }

        public void setNonHeapUsed(final Memory nonHeapUsed)
        {
            this.nonHeapUsed = nonHeapUsed;
        }

        public void setOsName(final String osName)
        {
            this.osName = osName;
        }

        public void setOsVersion(final String osVersion)
        {
            this.osVersion = osVersion;
        }

        public void setStartTime(final Date startTime)
        {
            this.startTime = startTime;
        }

        public void setTotalLoadedClasses(final long totalLoadedClasses)
        {
            this.totalLoadedClasses = totalLoadedClasses;
        }

        public void setTotalUnloadedClasses(final long totalUnloadedClasses)
        {
            this.totalUnloadedClasses = totalUnloadedClasses;
        }

        public void setUpTime(final Duration upTime)
        {
            this.upTime = upTime;
        }

        public void setVmArgs(final List<String> vmArgs)
        {
            this.vmArgs = vmArgs;
        }

        public void setVmName(final String vmName)
        {
            this.vmName = vmName;
        }

        public void setVmSpecVersion(final String vmSpecVersion)
        {
            this.vmSpecVersion = vmSpecVersion;
        }

        public void setVmVendor(final String vmVendor)
        {
            this.vmVendor = vmVendor;
        }

        public void setVmVersion(final String vmVersion)
        {
            this.vmVersion = vmVersion;
        }

        @Override
        public String toString()
        {
            final StringBuilder builder = new StringBuilder();
            builder.append("\nVM Name: ");
            builder.append(this.vmName);
            builder.append("\nVM Vendor: ");
            builder.append(this.vmVendor);
            builder.append("\nVM Version: ");
            builder.append(this.vmVersion);
            builder.append("\nStart Time: ");
            builder.append(this.startTime);
            builder.append("\nUp Time: ");
            builder.append(this.upTime);
            builder.append("\nVM Args: ");
            builder.append(this.vmArgs);
            builder.append("\nCurrently Loaded Classes: ");
            builder.append(this.currentlyLoadedClasses);
            builder.append("\nTotal Loaded Classes: ");
            builder.append(this.totalLoadedClasses);
            builder.append("\nTotal Unloaded Classes: ");
            builder.append(this.totalUnloadedClasses);
            builder.append("\nNative Architecture: ");
            builder.append(this.nativeArchitecture);
            builder.append("\nCPUs: ");
            builder.append(this.cpus);
            builder.append("\nOS Name: ");
            builder.append(this.osName);
            builder.append("\nOS Version: ");
            builder.append(this.osVersion);
            builder.append("\nHeap Initialized: ");
            builder.append(this.heapInitialized);
            builder.append("\nHeap Committed: ");
            builder.append(this.heapCommitted);
            builder.append("\nHeap Used: ");
            builder.append(this.heapUsed);
            builder.append("\nHeap Maximum: ");
            builder.append(this.heapMaximum);
            builder.append("\nNon Heap Initialized: ");
            builder.append(this.nonHeapInitialized);
            builder.append("\nNon Heap Committed: ");
            builder.append(this.nonHeapCommitted);
            builder.append("\nNon Heap Used: ");
            builder.append(this.nonHeapUsed);
            builder.append("\nNon Heap Maximum: ");
            builder.append(this.nonHeapMaximum);
            builder.append("\nMemory Pools: ");
            this.getMemoryPools().forEach(pool ->
            {
                builder.append(System.lineSeparator());
                builder.append(pool.toString("\t"));
                builder.append(System.lineSeparator());
            });
            builder.append("\nVM Spec Version: ");
            builder.append(this.vmSpecVersion);
            return builder.toString();
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(SystemInfo.class);

    public static SystemInfoBean buildSystemInfo()
    {
        final SystemInfoBean sib = new SystemInfoBean();
        systemInfo(sib);
        classLoadInfo(sib);
        operatingSystem(sib);
        memoryUse(sib);
        return sib;
    }

    public static void printSystemInfo()
    {
        logger.info(buildSystemInfo().toString());
    }

    protected static void classLoadInfo(final SystemInfoBean systemInfoBean)
    {
        final ClassLoadingMXBean bean = ManagementFactory.getClassLoadingMXBean();
        systemInfoBean.setCurrentlyLoadedClasses(bean.getLoadedClassCount());
        systemInfoBean.setTotalLoadedClasses(bean.getTotalLoadedClassCount());
        systemInfoBean.setTotalUnloadedClasses(bean.getUnloadedClassCount());
    }

    protected static void memoryUse(final SystemInfoBean systemInfoBean)
    {
        final MemoryMXBean bean = ManagementFactory.getMemoryMXBean();
        final MemoryUsage heapUsage = bean.getHeapMemoryUsage();
        final MemoryUsage nonHeapUsage = bean.getNonHeapMemoryUsage();
        systemInfoBean.setHeapCommitted(Memory.bytes(heapUsage.getCommitted()));
        systemInfoBean.setHeapInitialized(Memory.bytes(heapUsage.getInit()));
        systemInfoBean.setHeapMaximum(Memory.bytes(heapUsage.getMax()));
        systemInfoBean.setHeapUsed(Memory.bytes(heapUsage.getUsed()));
        systemInfoBean.setNonHeapCommitted(Memory.bytes(nonHeapUsage.getCommitted()));
        systemInfoBean.setNonHeapInitialized(Memory.bytes(nonHeapUsage.getInit()));
        systemInfoBean.setNonHeapMaximum(Memory.bytes(nonHeapUsage.getMax()));
        systemInfoBean.setNonHeapUsed(Memory.bytes(nonHeapUsage.getUsed()));
        systemInfoBean.add(ManagementFactory.getMemoryPoolMXBeans());
    }

    protected static void operatingSystem(final SystemInfoBean systemInfoBean)
    {
        final OperatingSystemMXBean bean = ManagementFactory.getOperatingSystemMXBean();
        systemInfoBean.setCpus(bean.getAvailableProcessors());
        systemInfoBean.setNativeArchitecture(bean.getArch());
        systemInfoBean.setOsName(bean.getName());
        systemInfoBean.setOsVersion(bean.getVersion());
    }

    protected static void systemInfo(final SystemInfoBean systemInfoBean)
    {
        final RuntimeMXBean bean = ManagementFactory.getRuntimeMXBean();
        systemInfoBean.setStartTime(new Date(bean.getStartTime()));
        systemInfoBean.setUpTime(Duration.milliseconds(bean.getUptime()));
        systemInfoBean.setVmArgs(bean.getInputArguments());
        systemInfoBean.setVmName(bean.getVmName());
        systemInfoBean.setVmVendor(bean.getVmVendor());
        systemInfoBean.setVmVersion(bean.getVmVersion());
        systemInfoBean.setVmSpecVersion(bean.getSpecVersion());
    }

    private SystemInfo()
    {
    }
}
