package gov.nasa.jpf.vm.va;

import java.util.*;

import javax.management.RuntimeErrorException;

import cmu.conditional.*;
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.va.IStackHandler.Type;
import de.fosd.typechef.conditional.Choice;
import de.fosd.typechef.featureexpr.FeatureExpr;
import de.fosd.typechef.featureexpr.FeatureExprFactory;
import gov.nasa.jpf.vm.Types;

class VStack implements IVStack {
public int size;
public Conditional<Entry>[] slots;
public Conditional<Integer>[] slot1;
Conditional<Integer> top = new One<>(0);
public FeatureExpr stackCTX;

public VStack() {
    size = -1;
    slots = (Conditional<Entry>[]) new Conditional[0];
    stackCTX = FeatureExprFactory.True();
}

public VStack(int nOperands) {
    size = -1;
    slots = (Conditional<Entry>[]) new Conditional[nOperands];
    stackCTX = FeatureExprFactory.True();
}

private void simpleCleanup() {
    while (size >= 0 && slots[size].equals(One.NULL))
        --size;
}

private void cleanup() {
    int i = 0, j = 0;
    while (i <= size) {
        // System.out.println("remove " + slots[i] + " " +
        // slots[i].equals(One.NULL));
        if (slots[i].equals(One.NULL)) {
            if (j <= i)
                j = i + 1;
            while (j <= size && slots[j].equals(One.NULL))
                j++;
            if (j > size) {
                size = i - 1;
                break;
            } else {
                slots[i++] = slots[j];
                slots[j++] = (Conditional<Entry>) One.NULL;
            }
        } else {
            i++;
        }
    }
}

/*
 * private void cleanup() { int i = 0, j = 0; while(i <= size) {
 * //System.out.println("remove " + slots[i] + " " + slots[i].equals(One.NULL));
 * if(slots[i].getFeatureExpr(null).isTautology()) { if(j <= i) j = i + 1;
 * while(j <= size && slots[j].getFeatureExpr(null).isTautology()) j++; if(j >
 * size) { size = i - 1; break; } else { slots[i++] = slots[j]; slots[j++] =
 * (Conditional<Entry>)One.NULL; } } else { i++; } } }
 */

public void clear(FeatureExpr ctx) {
    // System.out.println("clear " + ctx);
    simpleCleanup();
    pop(ctx, size);
    cleanup();
}

public FeatureExpr getCtx() {
    return stackCTX;
}

public void setCtx(FeatureExpr ctx) {
    // System.out.println("setCtx " + ctx + " " + stackCTX);
    stackCTX = ctx;
}

/**
 * hasAnyRef
 */

Boolean tmp = false;

public boolean hasAnyRef(FeatureExpr ctx) {
    Conditional<Boolean> res = new One<>(false);
    for (int i = 0; i <= this.size; i++) {
        final int j = i;
        res = res.mapfr(ctx, new BiFunction<FeatureExpr, Boolean, Conditional<Boolean>>() {
        public Conditional<Boolean> apply(FeatureExpr c, final Boolean x) {
            if (x == true) {
                return new One<>(true);
            }
            return slots[j].mapfr(c, new BiFunction<FeatureExpr, Entry, Conditional<Boolean>>() {
            public Conditional<Boolean> apply(FeatureExpr c, Entry y) {
                if (Conditional.isContradiction(c) || y == null) {
                    return new One<>(false);
                } else {
                    return new One<>(y.isRef);
                }
            }
            }).simplify();
        }
        }).simplify();
    }
    return res.simplify(ctx).getValue();
    // return res.getValue();
}

/**
 * push Operation related functions: add: deal with non-conditional value
 * pushEntry: push an Entry into stack push
 */
private void resize() {
    Conditional<Entry>[] tmp;
    // while(size >= 0 && slots[size].equals(One.NULL)) size--;
    if (size >= slots.length - 1) {
        tmp = (Conditional<Entry>[]) new Conditional[(size + 2) * 2];
        for (int i = 0; i <= size; ++i) {
            tmp[i] = slots[i];
        }
        slots = tmp;
        return;
    }

    if (slots.length > 1000 && size < slots.length / 4) {
        tmp = (Conditional<Entry>[]) new Conditional[slots.length / 2];
        for (int i = 0; i <= size; ++i) {
            tmp[i] = slots[i];
        }
        slots = tmp;
        return;
    }

}

private void add(FeatureExpr ctx, Integer value, boolean isRef) {
    resize();

    if (ctx.equivalentTo(stackCTX)) {
        slots[++size] = new One<>(new Entry(value, isRef));
    } else {
        slots[++size] = ChoiceFactory.create(ctx, new One<>(new Entry(value, isRef)), (Conditional<Entry>) One.NULL);
    }

    // if(slots[size].equals(One.NULL)) System.out.println("add null");

}

public void pushTrueEntry(final Conditional<Entry> value) {
    resize();
    slots[++size] = value;
}

public void pushEntry(final FeatureExpr ctx, final Conditional<Entry> value) {
    resize();

    if (ctx.equivalentTo(stackCTX)) {
        pushTrueEntry(value);
        return;
    }
    /*
     * slots[++size] = ((Conditional<Entry>)value).mapfr(ctx, new
     * BiFunction<FeatureExpr, Entry, Conditional<Entry>>() {
     * 
     * @Override public Conditional<Entry> apply(final FeatureExpr f, final
     * Entry value) { if (Conditional.isContradiction(f) || value == null) {
     * return (Conditional<Entry>)One.NULL; }
     * 
     * Entry e = value;
     * 
     * if (Conditional.isTautology(f)) { return new One<>(e); } return
     * ChoiceFactory.create(f, new One<>(e), (Conditional<Entry>)One.NULL); }
     * 
     * }).simplify();
     */
    // if(slots[size].equals(One.NULL)) System.out.println("pushEntry null");

    slots[++size] = ChoiceFactory.create(ctx, value, (Conditional<Entry>) One.NULL);
}

private boolean flag;

@SuppressWarnings("unchecked")
@Override
public void push(final FeatureExpr ctx, final Object value, final boolean isRef) {
    resize();
    if (value instanceof One) {
        push(ctx, ((One) value).getValue(), isRef);
    } else if (value instanceof Conditional) {
        flag = false;
        Conditional<Object> vs = ((Conditional<Object>) value).simplify(ctx);
        Conditional<Entry> tmp = vs.mapfr(null, new BiFunction<FeatureExpr, Object, Conditional<Entry>>() {

        @Override
        public Conditional<Entry> apply(final FeatureExpr f, final Object value) {
            Entry e;
            if (value instanceof Integer) {
                e = Entry.create((int) value, isRef);
            } else if (value instanceof Long) {
                flag = true;
                long v = ((Long) value).longValue();
                e = Entry.create((int) (v >> 32), isRef);
            } else if (value instanceof Double) {
                flag = true;
                long v = Double.doubleToLongBits((Double) value);
                e = Entry.create((int) (v >> 32), isRef);
            } else if (value instanceof Float) {
                e = Entry.create(Float.floatToIntBits((Float) value), isRef);
            } else if (value instanceof Byte) {
                e = Entry.create(((Byte) value).intValue(), isRef);
            } else if (value instanceof Short) {
                e = Entry.create((int) (Short) value, isRef);
            } else if (value == null) {
                e = Entry.create(MJIEnv.NULL, isRef);
            } else {
                throw new RuntimeException(value + " of type " + value.getClass() + " cannot be pushed to the stack.");
            }

            return new One<>(e);

        }
        });
        pushEntry(ctx, tmp);

        if (!flag)
            return;

        tmp = vs.mapfr(null, new BiFunction<FeatureExpr, Object, Conditional<Entry>>() {

        @Override
        public Conditional<Entry> apply(final FeatureExpr f, final Object value) {
            Entry e;

            if (value instanceof Long) {
                long v = ((Long) value).longValue();
                e = Entry.create((int) v, isRef);
            } else if (value instanceof Double) {
                long v = Double.doubleToLongBits((Double) value);
                e = Entry.create((int) v, isRef);
            } else {
                throw new RuntimeException(value + " of type " + value.getClass() + " cannot be pushed to the stack.");
            }

            return new One<>(e);

        }
        });
        pushEntry(ctx, tmp);

    } else {
        // System.out.println("push integer here");
        if (value instanceof Integer) {
            add(ctx, (Integer) value, isRef);
        } else if (value instanceof Long) {
            long v = ((Long) value).longValue();
            add(ctx, (int) (v >> 32), isRef);
            add(ctx, (int) v, isRef);
        } else if (value instanceof Double) {
            long v = Double.doubleToLongBits((Double) value);
            add(ctx, (int) (v >> 32), isRef);
            add(ctx, (int) v, isRef);
        } else if (value instanceof Float) {
            add(ctx, Float.floatToIntBits((Float) value), isRef);
        } else if (value instanceof Byte) {
            add(ctx, ((Byte) value).intValue(), isRef);
        } else if (value instanceof Short) {
            add(ctx, (int) (Short) value, isRef);
        } else if (value == null) {
            add(ctx, MJIEnv.NULL, isRef);
        } else {
            throw new RuntimeException(value + " of type " + value.getClass() + " cannot be pushed to the stack.");
        }
    }
}

public void fillholesHelper(final int count) {
    System.out.println("fillholesHelper");
    if (count == -1)
        return;
    slots[count] = slots[count]
            .mapfr(FeatureExprFactory.True(), new BiFunction<FeatureExpr, Entry, Conditional<Entry>>() {
            public Conditional<Entry> apply(FeatureExpr c, Entry e) {
                if (e == null) {
                    return popEntryFastHelper(c, count - 1);
                } else {
                    return new One<>(e);
                }
            }
            }).simplify();
    fillholesHelper(count - 1);
    return;
}

public void fillholes(FeatureExpr ctx) {
    for (int i = size; i >= 0; --i)
        fillholes(ctx, i);
    return;
}

public Conditional<Entry> peekHelper(final FeatureExpr ctx, final Conditional<Integer> offset) {
    fillholes(ctx);
    return offset.mapfr(ctx, new BiFunction<FeatureExpr, Integer, Conditional<Entry>>() {
    public Conditional<Entry> apply(FeatureExpr c, Integer offset) {
        if (Conditional.isContradiction(c) || offset == null) {
            return (Conditional<Entry>) One.NULL;
        } else {
            if (size - offset >= 0)
                return slots[size - offset].simplify(c);
            // return slots[size - offset];

            else
                return (Conditional<Entry>) One.NULL;
        }
    }
    }).simplify();
}

public Conditional<Integer> index2offset(final FeatureExpr ctx, final int index) {
    return topSet(ctx).mapfr(ctx, new BiFunction<FeatureExpr, Integer, Conditional<Integer>>() {
    public Conditional<Integer> apply(FeatureExpr c, Integer sz) {
        if (Conditional.isContradiction(c) || sz == null) {
            return (Conditional<Integer>) One.NULL;
        } else {
            if (index > sz)
                return (Conditional<Integer>) One.NULL;
            return new One<>(sz - index);
        }
    }
    }).simplify();

}

public void setRef(final FeatureExpr ctx, final int index, final boolean ref) {
    Conditional<Integer> offset = index2offset(ctx, index);
    // System.out.println("offset " + offset);
    fillholes(ctx);
    offset.mapfr(ctx, new VoidBiFunction<FeatureExpr, Integer>() {
    public void apply(FeatureExpr c, Integer offset) {
        // System.out.println("offset " + c + " " + offset);

        if (Conditional.isContradiction(c) || offset == null) {
            return;
        } else {
            if (offset > size)
                return;
            slots[size - offset] = slots[size - offset]
                    .mapfr(c, new BiFunction<FeatureExpr, Entry, Conditional<Entry>>() {
                    public Conditional<Entry> apply(FeatureExpr c, Entry e) {
                        // System.out.println(size + " " + c + " " + e);
                        if (Conditional.isContradiction(c) || e == null) {
                            return new One<>(e);
                        } else {
                            Entry ne = e.copy();
                            ne.isRef = ref;
                            return ChoiceFactory.create(c, new One<>(ne), new One<>(e));
                        }
                    }
                    }).simplify();
        }
    }
    });

}

public Conditional<Entry> get(final FeatureExpr ctx, final int index) {
    Conditional<Integer> offset = index2offset(ctx, index);
    return peekHelper(ctx, offset);
}

public Conditional<Integer> getInteger(final FeatureExpr ctx, final int index) {
    Conditional<Entry> res = get(ctx, index);
    return res.map(new Function<Entry, Integer>() {
    public Integer apply(Entry e) {
        return e.value;
    }
    }).simplify();
}

public boolean isRef(FeatureExpr ctx, int offset) {
    /*
     * if(ctx.isTautology()) { System.out.println("isRef " + ctx + " " +
     * offset); printStack(); }
     */
    try {
        return peek(ctx, offset).map(new Function<Entry, Boolean>() {
        public Boolean apply(Entry e) {
            if (e == null) {
                return false;
            } else {
                return e.isRef;
            }
        }
        }).simplify().getValue();
    } catch (Exception e) {
        printStack();
        return false;
    }

}

public boolean isRefLocal(FeatureExpr ctx, int index) {
    Conditional<Integer> offset = index2offset(ctx, index);
    return peekHelper(ctx, offset).map(new Function<Entry, Boolean>() {
    public Boolean apply(Entry e) {
        if (e == null) {
            return false;
        } else {
            return e.isRef;
        }
    }
    }).simplifyValues().getValue();
}

public void set(FeatureExpr ctx, final int offset, final int value, final boolean isRef) {
    // System.out.println("offset " + offset);
    fillholes(ctx);
    new One<Integer>(offset).mapfr(ctx, new VoidBiFunction<FeatureExpr, Integer>() {
    public void apply(FeatureExpr c, Integer offset) {
        // System.out.println("offset " + c + " " + offset);

        if (Conditional.isContradiction(c) || offset == null) {
            return;
        } else {
            if (offset > size)
                return;
            slots[size - offset] = slots[size - offset]
                    .mapfr(c, new BiFunction<FeatureExpr, Entry, Conditional<Entry>>() {
                    public Conditional<Entry> apply(FeatureExpr c, Entry e) {
                        // System.out.println(size + " " + c + " " + e);
                        if (Conditional.isContradiction(c) || e == null) {
                            return new One<>(e);
                        } else {
                            Entry ne = new Entry(value, isRef);
                            return ChoiceFactory.create(c, new One<>(ne), new One<>(e));
                        }
                    }
                    }).simplify();
        }
    }
    });

    // slots[top - offset] = new Entry(value, isRef);
}

public Map<Entry, FeatureExpr> mapMerge(Map<Entry, FeatureExpr> mapTmp) {
    Map<Entry, FeatureExpr> map = new HashMap<Entry, FeatureExpr>();
    for (Map.Entry<Entry, FeatureExpr> e : mapTmp.entrySet()) {
        FeatureExpr v = e.getValue();
        Entry k = e.getKey();
        FeatureExpr tmp = map.get(k);
        if (tmp != null) {
            map.put(k, tmp.or(v));
        } else {
            map.put(k, v);
        }
    }
    return map;
}

public Map<Entry, FeatureExpr> stackwidthHelper() {
    Map<Entry, FeatureExpr> map = new HashMap<>();
    for (int i = 0; i <= slots.length; i++) {
        map = mapMerge(slots[i].toMap());
    }
    return map;
}

VStack copy() {
    VStack clone = new VStack(slots.length);
    clone.size = this.size;
    for (int i = 0; i <= size; i++) {
        clone.slots[i] = slots[i];
    }
    return clone;
}

public boolean equals(Object o) {
    if (o == null) {
        return false;
    }
    if (o instanceof VStack) {
        if (((VStack) o).size == -1 && this.size == -1) {
            return true;
        }
        VStack s = this.copy();
        VStack t = ((VStack) o).copy();
        while (s.size != -1 && t.size != -1) {
            Conditional<Entry> st = s.pop(FeatureExprFactory.True());
            Conditional<Entry> tt = t.pop(FeatureExprFactory.True());
            if (!st.equals(tt))
                return false;
        }
        if (s.size == -1 && t.size == -1)
            return true;
    }
    return false;
}

/**
 * .. A B => .. B A B
 */
public void dup_x1(FeatureExpr ctx) {
    Conditional<Entry> b = pop(ctx);
    Conditional<Entry> a = pop(ctx);
    pushEntry(ctx, b);
    pushEntry(ctx, a);
    pushEntry(ctx, b);

}

/**
 * .. A B C D => .. C D A B C D
 */
public void dup2_x2(FeatureExpr ctx) {
    Conditional<Entry> d = pop(ctx);
    Conditional<Entry> c = pop(ctx);
    Conditional<Entry> b = pop(ctx);
    Conditional<Entry> a = pop(ctx);

    pushEntry(ctx, c);
    pushEntry(ctx, d);
    pushEntry(ctx, a);
    pushEntry(ctx, b);
    pushEntry(ctx, c);
    pushEntry(ctx, d);
}

/**
 * .. A B C => .. B C A B C
 */
public void dup2_x1(FeatureExpr ctx) {
    Conditional<Entry> c = pop(ctx);
    Conditional<Entry> b = pop(ctx);
    Conditional<Entry> a = pop(ctx);

    pushEntry(ctx, b);
    pushEntry(ctx, c);
    pushEntry(ctx, a);
    pushEntry(ctx, b);
    pushEntry(ctx, c);
}

/**
 * .. A B => .. A B A B
 */
public void dup2(FeatureExpr ctx) {
    cleanup();
    fillholes(ctx, size);
    fillholes(ctx, size - 1);
    Conditional<Entry> b = slots[size].simplify(ctx);
    Conditional<Entry> a = slots[size - 1].simplify(ctx);
    pushEntry(ctx, a);
    pushEntry(ctx, b);

    /*
     * Conditional<Entry> b = pop(ctx); Conditional<Entry> a = pop(ctx);
     * 
     * pushEntry(ctx, a); pushEntry(ctx, b); pushEntry(ctx, a); pushEntry(ctx,
     * b);
     */
}

/**
 * .. A => .. A A
 */
public void dup(FeatureExpr ctx) {
    fillholes(ctx, size);
    Conditional<Entry> a = slots[size].simplify(ctx);
    pushEntry(ctx, a);

    /*
     * Conditional<Entry> a = pop(ctx);
     * 
     * pushEntry(ctx, a); pushEntry(ctx, a);
     */
}

/**
 * .. A B C => .. C A B C
 */
public void dup_x2(FeatureExpr ctx) {
    Conditional<Entry> c = pop(ctx);
    Conditional<Entry> b = pop(ctx);
    Conditional<Entry> a = pop(ctx);

    pushEntry(ctx, c);
    pushEntry(ctx, a);
    pushEntry(ctx, b);
    pushEntry(ctx, c);
}

/**
 * .. A B => .. B A
 */
public void swap(FeatureExpr ctx) {
    cleanup();

    fillholes(ctx, size);
    fillholes(ctx, size - 1);

    Conditional<Entry>[] b = slots[size].split(ctx);
    Conditional<Entry>[] a = slots[size - 1].split(ctx);

    slots[size - 1] = ChoiceFactory.create(ctx, b[0], a[1]);
    slots[size] = ChoiceFactory.create(ctx, a[0], b[1]);
}

public String toString() {
    String s = "";
    for (int i = 0; i <= size; i++) {
        s += slots[i].toString() + "\n";
    }
    return s;
    // return slots[0].toString();
}

@Override
public void init(Stack st) {

}

@Override
public int getStackWidth() {
    // TODO Auto-generated method stub
    return 1;
}

@Override
public int[] getSlots(FeatureExpr ctx) {
    // TODO Auto-generated method stub
    return null;
}

@Override
public Set<Integer> getAllReferences() {
    final Set<Integer> references = new HashSet<Integer>();
    for (int i = 0; i <= size; i++) {
        slots[i].mapfr(FeatureExprFactory.True(), new VoidBiFunction<FeatureExpr, Entry>() {
        public void apply(FeatureExpr c, Entry e) {
            // System.out.println(size + " " + c + " " + e);
            if (e == null) {
                return;
            }
            if (e.isRef) {
                references.add(e.value);
            }
        }
        });
    }
    return references;
}

private <T> Conditional<Integer> topSize(final FeatureExpr ctx, final int count) {
    if (count == -1)
        return One.NEG_ONE;
    Conditional<Integer> tmp;
    int ret = 0;
    top = One.NEG_ONE;
    for (int i = 0; i <= count; i++) {
        final int j = i;
        if (slots[i].equals(One.NULL))
            continue;
        tmp = ChoiceFactory.create(slots[i].getFeatureExpr(null), One.ZERO, One.ONE).simplify(ctx);
        if (tmp.equals(One.ZERO))
            continue;
        if (tmp.equals(One.ONE)) {
            ++ret;
            continue;
        }
        top.fastApply(tmp, new BiFunction<Integer, Integer, Conditional<Integer>>() {
        public Conditional<Integer> apply(Integer x, Integer y) {
            return new One<>(x + y);
        }
        });
    }
    return top;
}

private <T> Conditional<Integer> topSet(final FeatureExpr ctx) {
    return topSize(ctx, size);
}

@Override
public Conditional<Integer> getTop() {
    return topSet(FeatureExprFactory.True());
}

/**
 * peek
 */

private void fillholes(final FeatureExpr ctx, final int count) {
    if (count == -1) {
        return;
    }

    FeatureExpr hole = slots[count].getFeatureExpr(null).and(ctx);

    if (hole.isContradiction())
        return;

    slots[count] = ChoiceFactory.create(hole, popEntryFastHelper(hole, count - 1), slots[count]);
    /*
     * slots[count] = slots[count].mapfr(ctx, new BiFunction<FeatureExpr, Entry,
     * Conditional<Entry>>() { public Conditional<Entry> apply(FeatureExpr c,
     * Entry e) { if (Conditional.isContradiction(c)) { return new One<>(e); }
     * if (e == null) { return ChoiceFactory.create(ctx,
     * popEntryFastHelper(c.and(ctx), count - 1), (Conditional<Entry>)
     * One.NULL); } else { return new One<>(e); } } }).simplify();
     */
    return;
}

private Conditional<Entry> peek(final FeatureExpr ctx, int offset) {
    if (size < offset) {
        return new One<>(new Entry(-1, false));
    }

    for (int i = 0; i <= offset && i <= size; ++i) {
        fillholes(ctx, size - i);
    }
    /*
     * if(ctx.isTautology()) { System.out.println("peek " + ctx + " " + offset);
     * printStack(); }
     */
    return slots[size - offset].simplify(ctx);
}

private Conditional<Integer> peekValue(final FeatureExpr ctx, int offset) {
    if (size < offset) {
        return new One<>(-1);
    }

    for (int i = 0; i <= offset && i <= size; ++i) {
        fillholes(ctx, size - i);
    }

    return slots[size - offset].simplify(ctx).map(new Function<Entry, Integer>() {
    public Integer apply(Entry e) {
        if (e == null)
            return null;
        return e.value;
    }
    }).simplify();
    /*
     * return peekHelper(ctx, new One<Integer>(offset)).map(new Function<Entry,
     * Integer>(){ public Integer apply(Entry e) { if(e == null) return null;
     * return e.value; } }).simplify();
     */
}

@Override
public <T> Conditional<T> peek(FeatureExpr ctx, int offset, Type t) {
    cleanup();
    switch (t) {
    case DOUBLE:
        final Conditional<Integer> tmp = peekValue(ctx, offset);
        final Conditional<Integer> tmp1 = peekValue(ctx, offset + 1);
        Conditional<T> res = tmp.mapfr(ctx, new BiFunction<FeatureExpr, Integer, Conditional<T>>() {
        public Conditional<T> apply(FeatureExpr c, final Integer x) {
            return tmp1.simplify(c).map(new Function<Integer, T>() {
            // return tmp1.map(new Function<Integer, T>(){
            public T apply(Integer y) {
                return (T) (Double) Types.intsToDouble(x, y);
            }
            }).simplify();
        }
        }).simplify();
        return res;
    // stack.peekValue(ctx, offset).
    // return (T) (Double) Types.intsToDouble(stack.peekValue(ctx,
    // offset).getValue(), stack.peekValue(ctx, offset + 1).getValue());
    case FLOAT:
        return peekValue(ctx, offset).map(new Function<Integer, T>() {
        public T apply(Integer x) {
            return (T) (Float) Types.intToFloat(x);
        }
        }).simplify().simplifyValues();
    // return (T) (Float) Types.intToFloat(stack.peek(offset));
    case INT:
        return peekValue(ctx, offset).map(new Function<Integer, T>() {
        public T apply(Integer x) {
            return (T) (Integer) x;
        }
        }).simplify();
    case LONG:
        final Conditional<Integer> tmp2 = peekValue(ctx, offset);
        final Conditional<Integer> tmp3 = peekValue(ctx, offset + 1);
        Conditional<T> res2 = tmp2.mapfr(ctx, new BiFunction<FeatureExpr, Integer, Conditional<T>>() {
        public Conditional<T> apply(FeatureExpr c, final Integer x) {
            return tmp3.simplify(c).map(new Function<Integer, T>() {
            // return tmp3.map(new Function<Integer, T>(){
            public T apply(Integer y) {
                return (T) (Long) Types.intsToLong(x, y);
            }
            }).simplify();
        }
        }).simplify();
        return res2;

    // return (T) (Long) Types.intsToLong(stack.peek(offset), stack.peek(offset
    // + 1));

    default:
        return null;
    }
    // return (Conditional<T> )stack.peek(ctx, offset);

}

/**
 * pop
 */
public Conditional<Entry> pop(final FeatureExpr ctx) {
    Conditional<Entry> ret = popEntryFastHelper(ctx, size);
    cleanup();
    return ret;
}

private Conditional<Integer> popInteger(final FeatureExpr ctx) {

    Conditional<Entry> ret = popEntryFastHelper(ctx, size);
    //System.out.println("ret " + ret);
    return ret.map(new Function<Entry, Integer>() {
    public Integer apply(Entry e) {
        return e.value;
    }
    }).simplify();
    // return ret;
}

private Conditional<Float> popFloat(final FeatureExpr ctx) {
    Conditional<Float> ret = popEntryFastHelper(ctx, size).map(new Function<Entry, Float>() {
    public Float apply(Entry e) {
        return (Float) Types.intToFloat(e.value);
    }
    }).simplify();
    return ret;
}

private Conditional<Entry> popHelper(FeatureExpr ctx, int count) {
    if(count == -1) return (Conditional<Entry>)One.NULL;
    Conditional<Entry> ret;
    Conditional<Entry>[] tmp = slots[count].split(ctx);
    slots[count] = tmp[1];
    FeatureExpr hole = tmp[0].getFeatureExpr(null);
    if(hole.isContradiction()) {
        return tmp[0];
    }
    return ChoiceFactory.create(hole.not(), tmp[0].simplify(hole.not()), popHelper(hole, count - 1));
}

private Conditional<Entry> popEntryFastHelper(FeatureExpr ctx, int count) {
    //System.out.println("popEntryFastHelper " + ctx + " " + (size - count));
    //printStack();
    // System.out.println("popfast");

    if (count == -1)
        return (Conditional<Entry>) One.NULL;

    Conditional<Entry>[][] tmp = new Conditional[count + 2][];

    FeatureExpr[] fs = new FeatureExpr[count + 2];

    FeatureExpr and = null;

    Conditional<Entry> ret;

    fs[count + 1] = ctx;

    int i;
    for (i = count; i >= 0; --i) {
        tmp[i] = slots[i].split(fs[i + 1]);
         System.out.println( tmp[i][0].getFeatureExpr(null));
//         System.out.println("" + slots[i] + " " + fs[i + 1] + " tmp " +
//         tmp[i][0] + " " + tmp[i][1]);
//              System.out.println("tmp " + tmp[i][0] + " " + tmp[i][0].getFeatureExpr(null));
        and = tmp[i][0].getFeatureExpr(null).and(fs[i + 1]);
        if (!fs[i + 1].equivalentTo(and)) {
            if (fs[i + 1].isTautology()) {
                slots[i] = (Conditional<Entry>) One.NULL;
            } else if (fs[i + 1].isContradiction()) {
                slots[i] = tmp[i][1].simplify();
            } else {
                slots[i] = ChoiceFactory.create(fs[i + 1].not(), tmp[i][1], (Conditional<Entry>) One.NULL).simplify();
            }
        }
        fs[i] = and;
        if (and.isContradiction())
            break;
        System.out.println("fs is " + i + " " + fs[i]);
    }
    /*
     * if(i == -1 && !(and.isContradiction())) { throw new
     * RuntimeErrorException(null, "and error"); //System.out.println(
     * "and error"); }
     */
    if (i == -1)
        i = 0;
    ret = tmp[i][0];
    

    for (int j = i + 1; j <= count; ++j) {
        //System.out.println(fs[j] + " " + ret + " " + tmp[j][0]);
        if (!tmp[j][0].equals(One.NULL))
            ret = ChoiceFactory.create(fs[j], ret, tmp[j][0].simplify(fs[j].not()));
            //ret = ChoiceFactory.create(fs[j], ret, tmp[j][0]);
    }
    // System.out.println("popEntryFastHelper result " + ret.simplify(ctx));
    // printStack();
    //System.out.println("ret " + ctx + " " + ret);
    return ret.simplify(ctx);
}

@Override
public Conditional<Entry> popEntry(FeatureExpr ctx, boolean copyRef) {
    simpleCleanup();
    // System.out.println("popEntry " + ctx + " " + copyRef);
    return popEntryFastHelper(ctx, size);
    // return pop(ctx);
}

private void popFastHelper(FeatureExpr ctx) {
    Conditional<Entry>[] tmp;
    for (int i = size; i >= 0; --i) {
        // System.out.println("before split " + ctx + " " + slots[i]);
        tmp = slots[i].split(ctx);
        // System.out.println("after split " + tmp[0]);
        // System.out.println("after split " + tmp[1]);
        // System.out.println("before and");

        FeatureExpr and = tmp[0].getFeatureExpr(null).and(ctx);
        // System.out.println("and " + and);
        if (!ctx.equivalentTo(and)) {
            // System.out.println("not equal");
            slots[i] = ChoiceFactory.create(ctx.not(), tmp[1], (Conditional<Entry>) One.NULL);
            // System.out.println("slot " + slots[i]);

            ctx = and;
        }
        if (ctx.isContradiction())
            break;
    }
}

@Override
public void pop(FeatureExpr ctx, int n) {
    // simpleCleanup();
    /*
     * if(n > 1) { System.out.println("pop " + ctx + " " + n); printStack(); }
     */
    simpleCleanup();
    for (int i = 0; i < n; i++) {
        popFastHelper(ctx);
        cleanup();
    }
}

@Override
public <T> Conditional<T> pop(FeatureExpr ctx, Type t) {
    Conditional<T> res = null;
    simpleCleanup();
    switch (t) {
    case INT:
        res = (Conditional<T>) popInteger(ctx);
        break;
    case FLOAT:
        res = (Conditional<T>) popFloat(ctx);
        break;
    case DOUBLE:
        final Conditional<Integer> tmp = popInteger(ctx);
        final Conditional<Integer> tmp1 = popInteger(ctx);
        res = (Conditional<T>) tmp.fastApply(tmp1, new BiFunction<Integer, Integer, Conditional<Double>>() {
        public Conditional<Double> apply(Integer x, Integer y) {
            return new One<>(Types.intsToDouble(x, y));
        }
        }).simplify();
        // res = Types.intsToDouble(lo, clone.pop());
        break;

    case LONG:
        final Conditional<Integer> reslong = popInteger(ctx);
        final Conditional<Integer> reslong1 = popInteger(ctx);
        res = (Conditional<T>) reslong.fastApply(reslong1, new BiFunction<Integer, Integer, Conditional<Long>>() {
        public Conditional<Long> apply(Integer x, Integer y) {
            return new One<>(Types.intsToLong(x, y));
        }
        }).simplify();
        break;
    // res = Types.intsToLong(lo, clone.pop());
    // break;
    default:
    }
    // return (Conditional<T>)stack.popInteger(ctx);
    cleanup();
    return res;
}

@Override
public void setTop(FeatureExpr ctx, int i) {
    // TODO Auto-generated method stub

}

@Override
public IVStack clone() {
    VStack clone = new VStack(slots.length);
    cleanup();
    clone.size = this.size;
    for (int i = 0; i <= size; i++) {
        clone.slots[i] = slots[i];
    }
    return clone;
}

@Override
public Conditional<Stack> getStack() {
    // TODO Auto-generated method stub
    return null;
}

private void printStack() {
    printStack(null);
}

private void printStack(String s) {
    if (s != null) {
        System.out.println(s);
    }
    for (int i = 0; i <= size; ++i) {
        System.out.println(slots[i]);
    }
}

}