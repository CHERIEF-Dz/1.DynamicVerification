package utils;

import ca.uqac.lif.cep.UniformProcessor;
import ca.uqac.lif.cep.functions.ApplyFunction;
import ca.uqac.lif.cep.functions.FunctionTree;
import ca.uqac.lif.cep.functions.StreamVariable;
import ca.uqac.lif.cep.tmf.Filter;
import ca.uqac.lif.cep.tmf.Fork;
import ca.uqac.lif.cep.tmf.QueueSource;
import ca.uqac.lif.cep.util.Booleans;
import ca.uqac.lif.cep.util.Strings;

import static ca.uqac.lif.cep.Connector.*;

public final class BeepBeepUtils {

    public static Filter codesmellConditions(UniformProcessor source, int arity, String[] events) {
        Fork forkEvents = new Fork(events.length+1);
        connect(source, arity, forkEvents, OUTPUT);

        FunctionTree orTree = new FunctionTree(Booleans.or, new StreamVariable(0), new StreamVariable(1));

        for (int i=2; i<events.length; i++) {
            orTree = new FunctionTree(Booleans.or, new StreamVariable(i), orTree);
        }
        ApplyFunction bigOr = new ApplyFunction(orTree);

        for (int i=0; i<events.length; i++) {
            ApplyFunction condition = new ApplyFunction(Strings.contains);
            connect(forkEvents, i, condition, 0);
            QueueSource conditionString = new QueueSource();
            conditionString.setEvents(events[i]);
            connect(conditionString, OUTPUT, condition, 1);
            connect(condition, OUTPUT, bigOr, i);
        }
        Filter filter = new Filter();
        connect(forkEvents, events.length, filter, LEFT);
        connect(bigOr, OUTPUT, filter, RIGHT);

        return filter;
    }
}
