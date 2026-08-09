#version 330

layout(std140) uniform DynamicTransforms {
    mat4 ModelViewMat;
    vec4 ColorModulator;
    vec3 ModelOffset;
    mat4 TextureMat;
};

in vec4 vertexColor;
in vec2 localUv;

out vec4 fragColor;

// Baked per-pipeline via RenderPipeline.Builder#withShaderDefine, because
// GuiElementRenderState offers no hook for per-draw uniforms.
#ifndef R_TL
#define R_TL 6.0
#endif
#ifndef R_TR
#define R_TR 6.0
#endif
#ifndef R_BR
#define R_BR 6.0
#endif
#ifndef R_BL
#define R_BL 6.0
#endif

// Signed distance to a rounded box centred on the origin, in pixels.
// Negative inside, positive outside.
float sdRoundedBox(vec2 p, vec2 halfSize, float r) {
    vec2 q = abs(p) - halfSize + r;
    return min(max(q.x, q.y), 0.0) + length(max(q, 0.0)) - r;
}

void main() {
    // Minecraft's GUI vertex formats carry no per-quad size, and NORMAL is
    // byte-normalised so it cannot smuggle pixel values. But localUv runs 0..1
    // across the quad, so its screen-space derivative is exactly 1/sizeInPixels.
    vec2 pxPerUv = 1.0 / fwidth(localUv);
    vec2 halfSize = pxPerUv * 0.5;
    vec2 p = (localUv - 0.5) * pxPerUv;

    // Each fragment lies in exactly one quadrant, and within a quadrant the
    // rounded-box distance depends only on that corner's radius -- so selecting
    // per-quadrant gives independent corners at no extra cost.
    // GUI space has y growing downward, so p.y > 0 is the bottom half.
    float r = (p.x > 0.0) ? ((p.y > 0.0) ? R_BR : R_TR)
                          : ((p.y > 0.0) ? R_BL : R_TL);

    // Never let a radius exceed half the shorter side.
    r = min(r, min(halfSize.x, halfSize.y));

    float dist = sdRoundedBox(p, halfSize, r);

    // One-pixel smoothstep across the boundary gives cheap analytic AA.
    float alpha = 1.0 - smoothstep(-0.5, 0.5, dist);
    if (alpha <= 0.0) {
        discard;
    }

    fragColor = vec4(vertexColor.rgb, vertexColor.a * alpha) * ColorModulator;
}
