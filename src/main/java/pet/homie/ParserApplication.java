package pet.homie;

import com.google.inject.Binder;
import com.google.inject.Module;
import io.bootique.Bootique;
import io.bootique.jersey.JerseyModule;

public class ParserApplication implements Module {

    public static void main(String[] args) {
        Bootique.app(args)
                .autoLoadModules()
                .modules(ParserApplication.class)
                .exec()
                .exit();
    }

    public void configure(Binder binder) {
        JerseyModule.extend(binder).addResource(ParserApi.class);
    }
}
