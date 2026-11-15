#version 330

in vec3 Position;
in vec2 UV0;

#moj_import <minecraft:projection.glsl>
#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <brandonscore:bc_params.glsl>

out vec3 fPos;
out vec2 texCoord0;

void main() {
    gl_Position = ProjMat * ModelViewMat * ModelMat * vec4(Position, 1.0);
    fPos = (ModelViewMat * ModelMat * vec4(Position, 1.0)).xyz;
    texCoord0 = UV0;
}
