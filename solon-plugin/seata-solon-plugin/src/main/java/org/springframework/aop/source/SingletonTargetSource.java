/*
 * Copyright 2002-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.aop.source;

import org.springframework.aop.TargetSource;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

import java.io.Serializable;

/**
 * Implementation of the {@link org.springframework.aop.TargetSource} interface
 * that holds a given object. This is the default implementation of the TargetSource
 * interface, as used by the Spring AOP framework. There is usually no need to
 * create objects of this class in application code.
 *
 * <p>This class is serializable. However, the actual serializability of a
 * SingletonTargetSource will depend on whether the target is serializable.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see org.springframework.aop.framework.AdvisedSupport#setTarget(Object)
 */
public class SingletonTargetSource implements TargetSource, Serializable {

    /** use serialVersionUID from Spring 1.2 for interoperability. */
    private static final long serialVersionUID = 9031246629662423738L;


    /** Target cached and invoked using reflection. */
    private final Object target;


    /**
     * Create a new SingletonTargetSource for the given target.
     * @param target the target object
     */
    public SingletonTargetSource(Object target) {
        Assert.notNull(target, "Target object must not be null");
        this.target = target;
    }


    @Override
    public Class<?> getTargetClass() {
        return this.target.getClass();
    }

    @Override
    public Object getTarget() {
        return this.target;
    }

    @Override
    public void releaseTarget(Object target) {
        // nothing to do
    }

    @Override
    public boolean isStatic() {
        return true;
    }


    /**
     * Two invoker interceptors are equal if they have the same target or if the
     * targets or the targets are equal.
     */
    @Override
    public boolean equals(Object other) {
        return (this == other || (other instanceof SingletonTargetSource &&
                this.target.equals(((SingletonTargetSource) other).target)));
    }

    /**
     * SingletonTargetSource uses the hash code of the target object.
     */
    @Override
    public int hashCode() {
        return this.target.hashCode();
    }

    @Override
    public String toString() {
        return "SingletonTargetSource for target object [" + ObjectUtils.identityToString(this.target) + "]";
    }

}
