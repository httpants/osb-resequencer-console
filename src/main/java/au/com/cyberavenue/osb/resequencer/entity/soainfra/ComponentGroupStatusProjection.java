package au.com.cyberavenue.osb.resequencer.entity.soainfra;

public interface ComponentGroupStatusProjection {

    String getComponentDn();

    Integer getReady();

    Integer getLocked();

    Integer getError();

    Integer getTimeout();

    Integer getSuspended();

    Integer getProcessing();

}
