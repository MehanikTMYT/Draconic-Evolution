// Shader-specific schema, exactly 176 bytes; BCShader only owns storage and snapshots.
layout(std140) uniform BCParams {
    mat4 ModelMat;          // 0
    vec4 BaseColour;        // 64
    vec4 EffectColourData;  // 80 (flash uses xy = position, zw = screen size)
    float Time;            // 96
    float Param0;          // 100: intensity / activation / fade
    float Power;           // 104
    float Startup;         // 108
    float Yaw;             // 112
    float Pitch;           // 116
    float Alpha;           // 120
    int Type;              // 124
    int BarMode;           // 128
    float Scale;           // 132
    float Intensity;       // 136: lens flare
    float DEPadding;       // 140
    vec4 FrameColourData;   // 144
    vec4 InnerTriColourData;// 160
};
#define time Time
#define intensity Param0
#define Activation Param0
#define Fade Param0
#define EffectColour EffectColourData.xyz
#define FrameColour FrameColourData.xyz
#define InnerTriColour InnerTriColourData.xyz
#define ScreenPos EffectColourData.xy
#define ScreenSize EffectColourData.zw
