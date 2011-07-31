/**
 * Copyright OPS4J
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ops4j.pax.wicket.impl14.internal;

import static java.lang.String.format;
import static org.osgi.framework.Constants.OBJECTCLASS;

import java.util.List;

import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.target.coding.IRequestTargetUrlCodingStrategy;
import org.ops4j.pax.wicket.api.Constants;
import org.ops4j.pax.wicket.impl14.api.MountPointInfo;
import org.ops4j.pax.wicket.impl14.api.PageMounter;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class PageMounterTracker extends ServiceTracker {

    private static final Logger LOGGER = LoggerFactory.getLogger(PageMounterTracker.class);

    private final WebApplication application;

    public PageMounterTracker(BundleContext context, WebApplication application, String applicationName)
        throws IllegalArgumentException {
        super(context, createFilter(context, applicationName), null);
        // validateNotNull(application, "application");
        this.application = application;
    }

    private static Filter createFilter(BundleContext context, String applicationName)
        throws IllegalArgumentException {
        // validateNotNull(context, "Context");
        // validateNotNull(applicationName, "applicationName");

        String filterString =
            "(&(" + OBJECTCLASS + "=" + PageMounter.class.getName() + ")"
                    + "(" + Constants.APPLICATION_NAME + "=" + applicationName + "))";

        try {
            return context.createFilter(filterString);
        } catch (InvalidSyntaxException e) {
            throw new IllegalStateException(format(
                "Application name [%s] is not allowed to contain spaces or special chars", applicationName), e);
        }
    }

    @Override
    public final Object addingService(ServiceReference reference) {
        PageMounter mounter = (PageMounter) super.addingService(reference);

        List<MountPointInfo> infos = mounter.getMountPoints();
        for (MountPointInfo info : infos) {
            IRequestTargetUrlCodingStrategy strategy = info.getCodingStrategy();
            LOGGER.trace("Make sure that path {} is clear before trying to remount", info.getPath());
            application.unmount(info.getPath());
            LOGGER.trace("Trying to mount {} with {}", info.getPath(), info.getCodingStrategy().toString());
            application.mount(strategy);
            LOGGER.info("Mounted {} with {}", info.getPath(), info.getCodingStrategy().toString());
        }

        return mounter;
    }

    @Override
    public final void removedService(ServiceReference reference, Object mounter) {
        PageMounter pageMounter = (PageMounter) mounter;
        List<MountPointInfo> infos = pageMounter.getMountPoints();
        for (MountPointInfo info : infos) {
            LOGGER.trace("Trying to mount {} with {}", info.getPath(), info.getCodingStrategy().toString());
            String path = info.getPath();
            application.unmount(path);
            LOGGER.info("Unmounted {} with {}", info.getPath(), info.getCodingStrategy().toString());
        }

        super.removedService(reference, pageMounter);
    }
}
