package es.gobcan.coetl.pentaho.enumeration;

public enum TransMethodsEnum implements CarteMethodsEnum {

    STATUS("transStatus/"), REGISTER("registerTrans/"), PREPARE("prepareExec/"), START("startExec/"), REMOVE("removeTrans/");

    private final String resource;

    private TransMethodsEnum(String resource) {
        this.resource = resource;
    }

    @Override
    public String getResource() {
        return this.resource;
    }
}
