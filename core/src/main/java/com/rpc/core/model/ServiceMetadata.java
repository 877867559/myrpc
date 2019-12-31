package com.rpc.core.model;

public class ServiceMetadata {

    private String directoryCache;

    private String group;               // 组别
    private String version = "1";             // 版本号暂时写死
    private String serviceProviderName; // 服务名称

    public ServiceMetadata() {}

    public ServiceMetadata(String group,  String serviceProviderName) {
       this(group,"1",serviceProviderName);
    }

    public ServiceMetadata(String group, String version, String serviceProviderName) {
        this.group = group;
        this.version = version;
        this.serviceProviderName = serviceProviderName;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getServiceProviderName() {
        return serviceProviderName;
    }

    public void setServiceProviderName(String serviceProviderName) {
        this.serviceProviderName = serviceProviderName;
    }

    public String metadata() {
        if (directoryCache != null) {
            return directoryCache;
        }

        StringBuilder buf = new StringBuilder();
        buf.append(getGroup())
                .append('-')
                .append(getVersion())
                .append('-')
                .append(getServiceProviderName());

        directoryCache = buf.toString();

        return directoryCache;
    }

    public void clear() {
        directoryCache = null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ServiceMetadata metadata = (ServiceMetadata) o;

        return group.equals(metadata.group)
                && version.equals(metadata.version)
                && serviceProviderName.equals(metadata.serviceProviderName);
    }

    @Override
    public int hashCode() {
        int result = group.hashCode();
        result = 31 * result + version.hashCode();
        result = 31 * result + serviceProviderName.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "ServiceMetadata{" +
                "group='" + group + '\'' +
                ", version='" + version + '\'' +
                ", serviceProviderName='" + serviceProviderName + '\'' +
                '}';
    }
}
