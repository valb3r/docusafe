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


BASETYPES_PACKAGE=org.adorsys.documentsafe.layer03business.types
ADAPTER_PACKAGE=org.adorsys.documentsafe.layer04rest.adapter

BASETYPES_DIR=$(echo $BASETYPES_PACKAGE | sed -e s#\\.#/#g)
ADAPTER_DIR=$(echo $ADAPTER_PACKAGE | sed -e s#\\.#/#g)


export timestamp=$(date "+%d.%m.%Y at %H:%M:%S")

export adaptertext=$(cat << EOF
package $ADAPTER_PACKAGE;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import $BASETYPES_PACKAGE.CLASSNAME;

/**
 * Created by peter on COPYDATE.
 */
public class CLASSNAMEJsonAdapter extends TypeAdapter<CLASSNAME> {
    @Override
    public void write(JsonWriter out, CLASSNAME value) throws IOException {
        out.value(value.getValue());
    }
    @Override
    public CLASSNAME read(JsonReader in) throws IOException {
        return new CLASSNAME(in.nextString());
    }
}
EOF)

echo "$adaptertext"   | sed s/CLASSNAME/$classname/g | sed s/BASETYPE/$type/g | sed s/BASETYP2/$type2/g | sed s/COPYDATE/"$timestamp"/g > src/main/java/$ADAPTER_DIR/${classname}JsonAdapter.java