package org.adorsys.documentsafe.layer02service.serializer;

import org.adorsys.documentsafe.layer00common.exceptions.SerializationException;

import java.util.HashMap;
import java.util.Map;

public class DocumentGuardSerializerRegistery {
	public static final String SERIALIZER_HEADER_KEY = "serilizer_id";

	private static DocumentGuardSerializerRegistery instance = new DocumentGuardSerializerRegistery();
	public static DocumentGuardSerializerRegistery getInstance(){
		return instance;
	}
	
	public Map<String, DocumentGuardSerializer> serializers = new HashMap<>();

	private DocumentGuardSerializerRegistery(){
		registerSerializer(DocumentGuardSerializer01.SERIALIZER_ID, new DocumentGuardSerializer01());
	}
	
	public DocumentGuardSerializer getSerializer(String serializerId){
		if(!serializers.containsKey(serializerId))
			throw new SerializationException("No Serializer with id : " + serializerId + " registered.");
		return serializers.get(serializerId);
	}
	
	public void registerSerializer(String serializerId, DocumentGuardSerializer serializer){
		serializers.put(serializerId, serializer);
	}
	
	public DocumentGuardSerializer defaultSerializer(){
		return serializers.get(DocumentGuardSerializer01.SERIALIZER_ID);
	}
}