package jsat.parameters;

import java.util.Arrays;
import java.util.List;
import jsat.math.decayrates.DecayRate;
import jsat.math.decayrates.ExponetialDecay;
import jsat.math.decayrates.InverseDecay;
import jsat.math.decayrates.LinearDecay;
import jsat.math.decayrates.NoDecay;

/**
 * A parameter for changing between the default {@link jsat.math.decayrates.DecayRate decay rates}.
 * 
 * @author Edward Raff
 */
public abstract class DecayRateParameter extends ObjectParameter<DecayRate>
{

    @Override
    public List<DecayRate> parameterOptions()
    {
        return Arrays.asList(new NoDecay(), new LinearDecay(), new ExponetialDecay(), new InverseDecay());
    }

    @Override
    public String getASCIIName()
    {
        return "Decay Rate";
    }
}
