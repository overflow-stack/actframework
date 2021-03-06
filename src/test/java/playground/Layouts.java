package playground;

import act.ActComponent;
import act.app.BuildFileProbe;
import act.app.ProjectLayout;

import java.util.List;

public enum Layouts {
    ;

    @ActComponent
    public static class MyStringParser extends BuildFileProbe.StringParser {

        @Override
        protected ProjectLayout parse(String fileContent) {
            return null;
        }

        @Override
        public String buildFileName() {
            return "mystring.layout";
        }
    }

    @ActComponent
    public static class MyLinesParser extends BuildFileProbe.LinesParser {
        @Override
        protected ProjectLayout parse(List<String> lines) {
            return null;
        }

        @Override
        public String buildFileName() {
            return "mylines.layout";
        }
    }

    public static void main(String[] args) {
        AbstractClassTest.foo();
    }
}
