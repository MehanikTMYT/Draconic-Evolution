#version 330

#moj_import <draconicevolution:de_effect_params.glsl>
#moj_import <minecraft:dynamictransforms.glsl>

#moj_import <minecraft:fog.glsl>
#moj_import <brandonscore:math.glsl>
#moj_import <brandonscore:chaos.glsl>

uniform sampler2D Sampler0;



in vec3 fPos;
in float vertexDistance;
in float vertexCylindricalDistance;
in vec4 vertexColor;
in vec2 texCoord0;
in vec4 normal;
in vec2 posMod;

out vec4 fragColor;

void main() {
    vec4 col = chaos(Sampler0, Time, Yaw, Pitch, Alpha, fPos, posMod);

    col *= vertexColor * ColorModulator;

    fragColor = apply_fog(col, vertexDistance, vertexCylindricalDistance, FogEnvironmentalStart, FogEnvironmentalEnd, FogRenderDistanceStart, FogRenderDistanceEnd, FogColor);
}
