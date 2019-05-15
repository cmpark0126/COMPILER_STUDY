abstract class EBNF {
    public abstract boolean match(InfoOfToken info);

    @Override
    public boolean equals(Object o){

        if(o == null) return false;
        InfoOfToken info = (InfoOfToken)o;
        if(match(info)) return true;
        return false;
    }
}
