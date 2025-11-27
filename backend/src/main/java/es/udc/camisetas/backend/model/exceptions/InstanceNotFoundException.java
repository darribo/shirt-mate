package es.udc.camisetas.backend.model.exceptions;

@SuppressWarnings("serial")
public class InstanceNotFoundException extends InstanceException {

  public InstanceNotFoundException(String name, Object key) {
    super(name, key);
  }

}
