package me.grishka.examples.pokedex.model;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import me.grishka.examples.pokedex.api.AllFieldsAreRequired;
import me.grishka.examples.pokedex.api.ObjectValidationException;
import me.grishka.examples.pokedex.api.RequiredField;

public abstract class BaseModel implements Cloneable{
	@CallSuper
	public void postprocess() throws ObjectValidationException{
		try{
			boolean allRequired=getClass().isAnnotationPresent(AllFieldsAreRequired.class);
			for(Field fld:getClass().getFields()){
				if(!fld.getType().isPrimitive() && !Modifier.isTransient(fld.getModifiers()) && (allRequired || fld.isAnnotationPresent(RequiredField.class))){
					if(fld.get(this)==null){
						throw new ObjectValidationException("Required field '"+fld.getName()+"' of type "+fld.getType().getSimpleName()+" was null in "+getClass().getSimpleName());
					}
				}
			}
		}catch(IllegalAccessException ignore){}
	}

	@NonNull
	@Override
	public Object clone(){
		try{
			return super.clone();
		}catch(CloneNotSupportedException x){
			throw new RuntimeException(x);
		}
	}
}
