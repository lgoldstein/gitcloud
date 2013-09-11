/*
 * Copyright 2013 Lyor Goldstein
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.validation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.collections15.ExtendedCollectionUtils;
import org.apache.commons.lang3.ExtendedStringUtils;
import org.springframework.util.Assert;

/**
 * @author Lyor G.
 */
public class SimpleErrors extends AbstractErrors {
    private static final long serialVersionUID = 4613630300148976293L;

    private String  objectName;
    private List<FieldError>  fieldErrors=new ArrayList<FieldError>();
    private List<ObjectError> globalErrors=new ArrayList<ObjectError>();

    protected SimpleErrors() {
        // for de-serializers
    }

    public SimpleErrors(String objName) {
        objectName = objName;
    }

    @Override
    public String getObjectName() {
        return objectName;
    }

    // for deserializers
    void setObjectName(String objName) {
        objectName = objName;
    }

    @Override
    public void reject(String errorCode, Object[] errorArgs, String defaultMessage) {
        ObjectError err=new ObjectError(getObjectName(), new String[] { errorCode }, errorArgs, defaultMessage);
        globalErrors.add(err);
    }

    @Override
    public void rejectValue(String field, String errorCode, Object[] errorArgs, String defaultMessage) {
        FieldError err=new FieldError(getObjectName(), field, null, false, new String[] { errorCode }, errorArgs, defaultMessage);
        fieldErrors.add(err);
    }

    @Override
    public void addAllErrors(Errors errors) {
        Assert.notNull(errors, "No errors to add");
        if (ExtendedStringUtils.safeCompare(getObjectName(), errors.getObjectName()) != 0) {
            throw new IllegalArgumentException("addErrors(" + getObjectName() + ") mismatched argument object name: " + errors.getObjectName());
        }

        Collection<? extends ObjectError>   glbl=errors.getGlobalErrors();
        if (ExtendedCollectionUtils.size(glbl) > 0) {
            globalErrors.addAll(glbl);
        }
        
        Collection<? extends FieldError>    flds=errors.getFieldErrors();
        if (ExtendedCollectionUtils.size(flds) > 0) {
            fieldErrors.addAll(flds);
        }
    }

    @Override
    public boolean hasErrors() {
        return (getErrorCount() > 0);
    }

    @Override
    public int getErrorCount() {
        return getGlobalErrorCount() + getFieldErrorCount();
    }

    @Override
    public boolean hasGlobalErrors() {
        return (getGlobalErrorCount() > 0);
    }

    @Override
    public int getGlobalErrorCount() {
        return ExtendedCollectionUtils.size(getGlobalErrors());
    }

    @Override
    public boolean hasFieldErrors() {
        return (getFieldErrorCount() > 0);
    }

    @Override
    public int getFieldErrorCount() {
        return ExtendedCollectionUtils.size(getFieldErrors());
    }

    @Override
    public List<ObjectError> getGlobalErrors() {
        return globalErrors;
    }

    @Override
    public List<FieldError> getFieldErrors() {
        return fieldErrors;
    }

    @Override
    public Object getFieldValue(String field) {
        FieldError fieldError = getFieldError(field);
        if (fieldError == null) {
            return null;
        } else {
            return fieldError.getRejectedValue();
        }
    }
}
