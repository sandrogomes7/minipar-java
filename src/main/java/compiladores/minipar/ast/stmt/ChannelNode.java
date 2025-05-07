package compiladores.minipar.ast.stmt;

import compiladores.minipar.ast.expr.ExprNode;

public abstract class ChannelNode extends StmtNode {
    private final String name;
    private final ExprNode host, port;
    protected ChannelNode(String name, ExprNode host, ExprNode port, int line) {
        super(line);
        this.name = name;
        this.host = host;
        this.port = port;
    }

    public String getName() {
        return name;
    }

    public ExprNode getHost() {
        return host;
    }

    public ExprNode getPort() {
        return port;
    }

    @Override
    public String toString() {
        return "channel " + name + " " +
               host.toString() + ":" + port.toString();
    }
}
