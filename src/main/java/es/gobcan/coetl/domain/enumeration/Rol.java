package es.gobcan.coetl.domain.enumeration;

public enum Rol {
	// El rol de administrador no es un rol al uso porque los roles van en función del organismo. 
	// Un administrador se establece con un booleano en el usuario
    ADMIN, 
    TECNICO, 
    LECTOR
}
