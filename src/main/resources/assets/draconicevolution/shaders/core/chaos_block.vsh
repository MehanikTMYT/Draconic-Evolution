#version 330

#moj_import <draconicevolution:de_effect_params.glsl>
#moj_import <minecraft:projection.glsl>
#moj_import <minecraft:dynamictransforms.glsl>

#moj_import <minecraft:fog.glsl>

in vec3 Position;
in vec4 Color;
in vec2 UV0;
in ivec2 UV2;
in vec3 Normal;

uniform sampler2D Sampler2;


out vec3 fPos;
out float vertexDistance;
out float vertexCylindricalDistance;
out vec4 vertexColor;
out vec2 texCoord0;
out vec4 normal;
out vec2 posMod;

void main() {
    fPos = (ModelViewMat * ModelMat * vec4(Position, 1.0)).xyz;
    gl_Position = ProjMat * ModelViewMat * ModelMat * vec4(Position, 1.0);

//    vertexDistance = fog_distance(ModelViewMat, Position, FogShape);
    vertexDistance = fog_spherical_distance(fPos);
    vertexCylindricalDistance = fog_cylindrical_distance(fPos);
    vertexColor = Color * texelFetch(Sampler2, UV2 / 16, 0);
    texCoord0 = UV0;
    normal = ProjMat * ModelViewMat * ModelMat * vec4(Normal, 0.0);
    posMod = normalize(normal).xy / 100;
}
