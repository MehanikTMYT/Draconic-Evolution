package com.brandon3055.draconicevolution.client.shader;

import com.brandon3055.brandonscore.client.shader.BCShader;
import java.util.Objects;

/** Typed view of a documented std140 member in the 176 byte extraction block. */
public final class EffectUniform {
    private final BCShader<?> shader;
    private final int offset, components;
    private final boolean integer;
    public EffectUniform(BCShader<?> shader, int offset, int components, boolean integer) {
        Objects.checkFromIndexSize(offset, components * Float.BYTES, 176);
        if ((offset & 3) != 0 || components < 1 || components > 4) throw new IllegalArgumentException("Invalid uniform member");
        this.shader = shader; this.offset = offset; this.components = components; this.integer = integer;
    }
    private void check(int count, boolean isInteger) {
        if (count != components || isInteger != integer) throw new IllegalArgumentException("Uniform type mismatch");
    }
    public void glUniform1f(float value) { check(1, false); shader.uniform(offset).glUniform1f(value); }
    public void glUniform1i(int value) { check(1, true); shader.uniform(offset).glUniform1i(value); }
    public void glUniform2f(float x, float y) {
        check(2, false); shader.uniform(offset).glUniform1f(x); shader.uniform(offset + 4).glUniform1f(y);
    }
    public void glUniform3f(float x, float y, float z) {
        check(3, false); shader.uniform(offset).glUniform1f(x); shader.uniform(offset + 4).glUniform1f(y); shader.uniform(offset + 8).glUniform1f(z);
    }
    public void glUniform4f(float x, float y, float z, float w) {
        check(4, false); shader.uniform(offset).glUniform4f(x, y, z, w);
    }
}
