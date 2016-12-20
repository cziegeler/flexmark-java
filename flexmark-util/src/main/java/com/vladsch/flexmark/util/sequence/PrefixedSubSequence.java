package com.vladsch.flexmark.util.sequence;

/**
 * A CharSequence that references original char sequence, maps '\0' to '\uFFFD' and is prefixed with a fixed string
 * a subSequence() returns a sub-sequence from the original base sequence, possibly with a prefix if it falls in range
 */
public class PrefixedSubSequence extends BasedSequenceImpl {
    private final String prefix;
    private final int prefixLength;
    private final BasedSequence base;

    public PrefixedSubSequence(String prefix, BasedSequence charSequence) {
        this(prefix, charSequence, 0, charSequence.length());
    }

    @Override
    public CharSequence getBase() {
        return base.getBase();
    }

    @Override
    public int getStartOffset() {
        return base.getStartOffset();
    }

    @Override
    public int getEndOffset() {
        return base.getEndOffset();
    }

    @Override
    public Range getSourceRange() {
        return base.getSourceRange();
    }

    @Override
    public BasedSequence baseSubSequence(int start, int end) {
        return null;
    }

    public PrefixedSubSequence(String prefix, BasedSequence baseSeq, int start, int end) {
        this(prefix, baseSeq, start, end, true);
    }

    private PrefixedSubSequence(String prefix, BasedSequence baseSeq, int start, int end, boolean replaceChar) {
        this.prefix = replaceChar ? prefix.replace('\0', '\uFFFD') : prefix;
        this.prefixLength = prefix.length();
        this.base = baseSeq.subSequence(start, end);
    }

    @Override
    public int length() {
        return prefixLength + base.length();
    }

    @Override
    public int getIndexOffset(int index) {
        if (index < prefixLength) {
            // KLUDGE: to allow creation of segmented sequences that have prefixed characters not from original base
            return -1;
        }
        return base.getIndexOffset(index - prefixLength);
    }

    @Override
    public char charAt(int index) {
        if (index >= 0 && index < base.length() + prefixLength) {
            if (index < prefixLength) {
                return prefix.charAt(index);
            } else {
                return base.charAt(index - prefixLength);
            }
        }
        throw new StringIndexOutOfBoundsException("String index out of range: " + index);
    }

    @Override
    public BasedSequence subSequence(int start, int end) {
        if (start >= 0 && end <= base.length() + prefixLength) {
            if (start < prefixLength) {
                if (end <= prefixLength) {
                    // all from prefix
                    return new PrefixedSubSequence(prefix.substring(start, end), base.subSequence(0, 0), 0, 0, false);
                } else {
                    // some from prefix some from base
                    return new PrefixedSubSequence(prefix.substring(start), base, 0, end - prefixLength, false);
                }
            } else {
                // all from base
                return base.subSequence(start - prefixLength, end - prefixLength);
            }
        }
        if (start < 0 || start > base.length() + prefixLength) {
            throw new StringIndexOutOfBoundsException("String index out of range: " + start);
        }
        throw new StringIndexOutOfBoundsException("String index out of range: " + end);
    }

    @Override
    public String toString() {
        return prefix + String.valueOf(base);
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this || (obj instanceof CharSequence && toString().equals(obj.toString()));
    }
}
