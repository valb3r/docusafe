if [[ -z $1 ]]
then
    echo "please pass parameter classname "
    exit 1
fi
export classname=$1

if [[ -z $2 ]]
then
    echo "please pass second parameter type "
    exit 1
fi
export type="invalid"

if [[ $2 == "int" ]]
then
    type="Integer"
fi

if [[ $2 == "long" ]]
then
    type="Long"
fi

if [[ $2 == "bool" ]]
then
    type="Boolean"
fi

if [[ $2 == "double" ]]
then
    type="Double"
fi

if [[ $2 == "str" ]]
then
    type="String"
fi

if [[ $2 == "byte" ]]
then
    type="ByteArray"
    type2="byte[]"
fi

if [[ $type == "invalid" ]]
then
    echo "please enter second param as int, long, bool, double byte or str"
    exit 1
fi

if [[ -z $type2 ]]
then
    type2=$type
fi


BASETYPES_PACKAGE=org.adorsys.resource.server.basetypes
ADAPTER_PACKAGE=org.adorsys.resource.server.basetypes.adapter
CONVERTER_PACKAGE=org.adorsys.resource.server.basetypes.converter

BASETYPES_DIR=$(echo $BASETYPES_PACKAGE | sed -e s#\\.#/#g)
ADAPTER_DIR=$(echo $ADAPTER_PACKAGE | sed -e s#\\.#/#g)
CONVERTER_DIR=$(echo $CONVERTER_PACKAGE | sed -e s#\\.#/#g)


export timestamp=$(date "+%d.%m.%Y at %H:%M:%S")


export classtext=$(cat << EOF
package $BASETYPES_PACKAGE;

import $ADAPTER_PACKAGE.CLASSNAMERestAdapter;

import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * Created by peter on COPYDATE.
 */
@XmlJavaTypeAdapter(CLASSNAMERestAdapter.class)
@XmlType
public class CLASSNAME extends BaseTypeBASETYPE {
    public CLASSNAME() {}

    public CLASSNAME(BASETYP2 value) {
        super(value);
    }
}
EOF)

export adaptertext=$(cat << EOF
package $ADAPTER_PACKAGE;

import $BASETYPES_PACKAGE.CLASSNAME;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Created by peter on COPYDATE.
 */
public class CLASSNAMERestAdapter extends XmlAdapter<BASETYP2, CLASSNAME> {
    @Override
    public CLASSNAME unmarshal(BASETYP2 value) {
        return new CLASSNAME(value);
    }

    @Override
    public BASETYP2 marshal(CLASSNAME value) {
        return (value != null) ? value.getValue() : null;
    }
}
EOF)

export convertertext=$(cat << EOF
package $CONVERTER_PACKAGE;

import $BASETYPES_PACKAGE.CLASSNAME;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * Created by peter on COPYDATE.
 */
@Converter(autoApply = true)
public class CLASSNAMEDBConverter implements AttributeConverter<CLASSNAME, BASETYP2> {
    @Override
    public BASETYP2 convertToDatabaseColumn(CLASSNAME v) {
        return v != null ? v.getValue() : null;
    }

    @Override
    public CLASSNAME convertToEntityAttribute(BASETYP2 v) {
        return v != null ? new CLASSNAME(v) : null;
    }
}
EOF)

echo "$classtext"     | sed s/CLASSNAME/$classname/g | sed s/BASETYPE/$type/g | sed s/BASETYP2/$type2/g | sed s/COPYDATE/"$timestamp"/g > src/main/java/$BASETYPES_DIR/$classname.java

echo "$adaptertext"   | sed s/CLASSNAME/$classname/g | sed s/BASETYPE/$type/g | sed s/BASETYP2/$type2/g | sed s/COPYDATE/"$timestamp"/g > src/main/java/$ADAPTER_DIR/${classname}RestAdapter.java

echo "$convertertext" | sed s/CLASSNAME/$classname/g | sed s/BASETYPE/$type/g | sed s/BASETYP2/$type2/g | sed s/COPYDATE/"$timestamp"/g > src/main/java/$CONVERTER_DIR/${classname}DBConverter.java