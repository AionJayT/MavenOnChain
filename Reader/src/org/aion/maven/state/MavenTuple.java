package org.aion.maven.state;


/**
 * The group-artifact-version tuple used by Maven.
 * Note:  These are expected to be used as keys in data structures so equals() and hashcode() are implemented.
 */
public final class MavenTuple {
    public final String groupId;
    public final String artifactId;
    public final String version;
    public final String type;   // (pom/jar)

    public MavenTuple(String groupId, String artifactId, String version, String type) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.type = type;
    }

    @Override
    public int hashCode() {
        return this.groupId.hashCode()
                ^ this.artifactId.hashCode()
                ^ this.version.hashCode()
                ^ this.type.hashCode()
        ;
    }

    @Override
    public boolean equals(Object obj) {
        boolean isEqual = (this == obj);
        if (!isEqual && (null != obj) && (getClass() == obj.getClass())) {
            MavenTuple other = (MavenTuple) obj;
            isEqual = this.groupId.equals(other.groupId)
                    && this.artifactId.equals(other.artifactId)
                    && this.version.equals(other.version)
                    && this.type.equals(other.type)
            ;
        }
        return isEqual;
    }

    @Override
    public String toString() {
        return "MavenTuple(groupId=" + this.groupId
                + ", artifactId=" + this.artifactId
                + ", version=" + this.version
                + ", type=" + this.type
        + ")";
    }
}
