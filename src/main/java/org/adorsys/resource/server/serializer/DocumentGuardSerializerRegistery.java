package org.adorsys.resource.server.serializer;

import java.nio.channels.IllegalSelectorException;
import java.util.HashMap;
import java.util.Map;

import org.adorsys.encobject.service.ObjectNotFoundException;

public class DocumentGuardSerializerRegistery {
	
	private static DocumentGuardSerializerRegistery instance = new DocumentGuardSerializerRegistery();
	public static DocumentGuardSerializerRegistery getInstance(){
		return instance;
	}
	
	public Map<String, DocumentGuardSerializer> serializers = new HashMap<>();

	private DocumentGuardSerializerRegistery(){
		registerSerializer(DocumentGuardSerializer01.SERIALIZER_ID, new DocumentGuardSerializer01());
	}
	
	public DocumentGuardSerializer getSerializer(String serializerId){
		if(!serializers.containsKey(serializerId)) throw new IllegalStateException("No Serializer with id : " + serializerId + " registered.");
		return serializers.get(serializerId);
	}
	
	public void registerSerializer(String serializerId, DocumentGuardSerializer serializer){
		serializers.put(serializerId, serializer);
	}
}
