abstract class EBNF {
    public abstract boolean match(String token);

    @Override
    public boolean equal(String token){
        if(token == null) return false;
        if(match(token)) return true;
    }
}

abstract class Terminal extends EBNF{
}

abstract class NonTerminal extends EBNF{
    public abstract boolean dfa(char ch, int current_state);
}
