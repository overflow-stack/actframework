package act.app;

import act.Act;
import act.util.DestroyableBase;
import org.osgl.$;
import org.osgl.logging.LogManager;
import org.osgl.logging.Logger;
import org.osgl.util.C;
import org.osgl.util.E;

import javax.enterprise.context.ApplicationScoped;
import java.util.Iterator;
import java.util.Map;

import static act.Destroyable.Util.tryDestroyAll;

/**
 * Manage applications deployed on Act
 */
public class AppManager extends DestroyableBase {

    private static Logger logger = LogManager.get(AppManager.class);

    private Map<Integer, App> byPort = C.newMap();
    private Map<String, App> byContextPath = C.newMap();

    private AppManager() {
    }

    @Override
    protected void releaseResources() {
        tryDestroyAll(byPort.values(), ApplicationScoped.class);
        byPort = null;

        tryDestroyAll(byContextPath.values(), ApplicationScoped.class);
        byPort = null;
    }

    public AppManager scan() {
        Act.mode().appScanner().scan(null, _F.loadApp(this));
        return this;
    }

    public AppManager loadSingleApp(String name) {
        AppScanner.SINGLE_APP_SCANNER.scan(name, _F.loadApp(this));
        return this;
    }

    public void deploy(App app) {
        load(app);
        refresh(app);
    }

    public void refresh() {
        Iterator<App> itr = appIterator();
        while (itr.hasNext()) {
            refresh(itr.next());
        }
    }

    public void refresh(App app) {
        app.refresh();
    }

    public void load(App app) {
        app.build();
        app.refresh();
        int port = app.config().httpPort();
        if (port < 0) {
            loadIntoContextMap(app.config().urlContext(), app);
        } else {
            loadIntoPortMap(port, app);
        }
        app.hook();
    }

    public boolean unload(App app) {
        boolean b = unloadApp(app, byPort);
        if (!b) {
            b = unloadApp(app, byContextPath);
        }
        if (byPort.isEmpty() && byContextPath.isEmpty()) {
            Act.shutdown();
        }
        return b;
    }

    private boolean unloadApp(App app, Map<?, App> map) {
        for (Map.Entry<?, App> entry : map.entrySet()) {
            if (app == entry.getValue()) {
                app.destroy();
                map.remove(entry.getKey());
                return true;
            }
        }
        return false;
    }

    private void loadIntoPortMap(int port, App app) {
        App app0 = byPort.get(port);
        if (null != app0) {
            E.invalidConfigurationIf(!app.equals(app0), "Another application has already been deployed using port %s", port);
        } else {
            byPort.put(port, app);
        }
    }

    private void loadIntoContextMap(String context, App app) {
        App app0 = byContextPath.get(context);
        if (null != app0) {
            E.invalidConfigurationIf(!app.equals(app0), "Another application has already been deployed using context %s", context);
        } else {
            byContextPath.put(context, app);
        }
    }

    private Iterator<App> appIterator() {
        final Iterator<App> itrByPort = byPort.values().iterator();
        final Iterator<App> itrByContext = byContextPath.values().iterator();
        return new Iterator<App>() {
            boolean byPortFinished = !itrByPort.hasNext();

            @Override
            public boolean hasNext() {
                if (!byPortFinished) {
                    byPortFinished = !itrByPort.hasNext();
                }
                return !byPortFinished || itrByContext.hasNext();
            }

            @Override
            public App next() {
                return byPortFinished ? itrByContext.next() : itrByPort.next();
            }

            @Override
            public void remove() {
                E.unsupport();
            }
        };
    }

    public static AppManager create() {
        return new AppManager();
    }

    private enum _F {
        ;

        static final $.F1<App, ?> loadApp(final AppManager mgr) {
            return new $.Visitor<App>() {
                @Override
                public void visit(App app) throws $.Break {
                    try {
                        mgr.load(app);
                    } catch (RuntimeException e) {
                        Act.shutdownApp(app);
                        throw e;
                    }
                }
            };
        }
    }
}
