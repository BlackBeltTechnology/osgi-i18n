package hu.blackbelt.osgi.i18n.resourcebundle.utils;

public final class SimpleParameterTemplater {
    private SimpleParameterTemplater() {
    }

    public static String generateAutoTemplate(String prefix, Object...args) {
        String template = prefix;
        if (args != null) {
            for (int i = 0; i < args.length; i++) {
                template = template + " {" + i + "}";
            }
        }
        return template;
    }

    public static String templateMessage(String tmpl, Object...args) {
        String template = tmpl;
        if (args != null) {
            for (int i = 0; i < args.length; i++) {
                String value = args[i] == null ? "null" : args[i].toString();
                String replacedPattern = null;
                replacedPattern = "\\{" + i + "\\}";
                if (replacedPattern != null) {
                    template = template.replaceAll(replacedPattern, value == null ? "" : value);
                }
            }
        }
        return template;
    }

}
