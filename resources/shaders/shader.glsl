//$Vertex Shader
#version 330

layout (location = 0) in vec3 vPos;
layout (location = 1) in vec3 vCol;

out vec3 fCol;

void main() {
    gl_Position = vec4(vPos, 1.);
    fCol = vCol;
}

//$Fragment Shader
#version 330

in vec3 fCol;

void main() {
    gl_FragColor = vec4(fCol, 1.0);
}