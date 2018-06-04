/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.graphics;

/**
 * Creates a polygon from a path
 */
public class PolygonParser {
    enum Token {EOF, COMMAND, NUMBER}

    private final String path;
    private int lastTokenPos;
    private int pos;
    private char command;
    private float value;
    private float x;
    private float y;

    /**
     * Creates a new instance
     *
     * @param path the path to parse
     */
    public PolygonParser(String path) {
        this.path = path;
        pos = 0;
    }

    Token next() {
        lastTokenPos = pos;
        while (pos < path.length() && (path.charAt(pos) == ' ' || path.charAt(pos) == ','))
            pos++;
        if (pos == path.length())
            return Token.EOF;

        char c = path.charAt(pos);
        if (Character.isAlphabetic(c)) {
            pos++;
            command = c;
            return Token.COMMAND;
        } else {
            int p0 = pos;
            while (pos < path.length() && isNumChar(path.charAt(pos)))
                pos++;

            String numStr = path.substring(p0, pos);
            value = Float.parseFloat(numStr);
            return Token.NUMBER;
        }
    }

    private void unreadToken() {
        pos = lastTokenPos;
    }

    char getCommand() {
        return command;
    }

    double getValue() {
        return value;
    }

    private boolean isNumChar(char c) {
        return Character.isDigit(c) || c == '.' || c == '-' || c == '+' || c == 'e';
    }

    private float nextValue() throws ParserException {
        if (next() != Token.NUMBER)
            throw new ParserException("expected a number at pos " + pos + " in '" + path + "'");
        return value;
    }

    private VectorFloat nextVector() throws ParserException {
        x = nextValue();
        y = nextValue();
        return new VectorFloat(x, y);
    }

    private VectorFloat nextVectorInc() throws ParserException {
        x += nextValue();
        y += nextValue();
        return new VectorFloat(x, y);
    }

    /**
     * Creates a polygon from the given path
     *
     * @return the polygon
     * @throws ParserException ParserException
     */
    public Polygon create() throws ParserException {
        Polygon p = new Polygon(false);
        Token tok;
        while ((tok = next()) != Token.EOF) {
            if (tok == Token.NUMBER) {
                unreadToken();
                if (command == 'm')
                    command = 'l';
                else if (command == 'M')
                    command = 'L';
            }
            switch (command) {
                case 'M':
                    p.add(nextVector());
                    break;
                case 'm':
                    p.add(nextVectorInc());
                    break;
                case 'V':
                    y = nextValue();
                    p.add(new VectorFloat(x, y));
                    break;
                case 'v':
                    y += nextValue();
                    p.add(new VectorFloat(x, y));
                    break;
                case 'H':
                    x = nextValue();
                    p.add(new VectorFloat(x, y));
                    break;
                case 'h':
                    x += nextValue();
                    p.add(new VectorFloat(x, y));
                    break;
                case 'l':
                    p.add(nextVectorInc());
                    break;
                case 'L':
                    p.add(nextVector());
                    break;
                case 'c':
                    p.add(nextVectorInc(), nextVectorInc(), nextVectorInc());
                    break;
                case 'C':
                    p.add(nextVector(), nextVector(), nextVector());
                    break;
                case 'a':
                    addArc(p, nextVectorInc(), nextValue(), nextValue() != 0, nextValue() != 0, nextVectorInc());
                    break;
                case 'A':
                    addArc(p, nextVector(), nextValue(), nextValue() != 0, nextValue() != 0, nextVector());
                    break;
                case 'Z':
                case 'z':
                    p.setClosed(true);
                    break;
                default:
                    throw new ParserException("unsupported path command " + command);
            }
        }
        return p;

    }

    private void addArc(Polygon p, VectorFloat rad, float rot, boolean large, boolean sweep, VectorFloat pos) {
        p.add(pos);
    }

    /**
     * The parser exception
     */
    public static final class ParserException extends Exception {
        private ParserException(String message) {
            super(message);
        }
    }

}
