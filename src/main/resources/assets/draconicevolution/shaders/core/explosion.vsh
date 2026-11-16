#version 330

#moj_import <draconicevolution:de_effect_params.glsl>
#moj_import <minecraft:projection.glsl>
#moj_import <minecraft:dynamictransforms.glsl>

in vec3 Position;
in vec2 UV0;


out vec2 texCoord0;

void main() {
    gl_Position = ProjMat * ModelViewMat * ModelMat * vec4(Position, 1.0);
    texCoord0 = UV0;
}
