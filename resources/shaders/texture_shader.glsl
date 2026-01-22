//$Vertex Shader
#version 330

layout (location = 0) in vec3 vPos; //$ Position
layout (location = 1) in vec2 vTex; //$ Texture $InvertedXY

out vec2 fTex;

uniform mat4 view;
uniform mat4 projection;
uniform mat4 transform;

void main() {
    gl_Position = projection * view * transform * vec4(vPos, 1.);
    fTex = vTex;
}

//$Fragment Shader
#version 330

in vec2 fTex;

uniform sampler2D image;

void main() {
    gl_FragColor = texture(image, fTex);
}