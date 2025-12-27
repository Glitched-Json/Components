//$Vertex Shader
#version 330

layout (location = 0) in vec3 vPos; //$ Position
layout (location = 1) in vec3 vCol; //$ Color

out vec3 fCol;

uniform mat4 view; //$ uniformView
uniform mat4 projection; //$ uniformProjection

void main() {
    gl_Position = projection * view * (vec4(vPos, 1.) + vec4(0, 0, -3, 0));
    fCol = vCol;
}

//$Fragment Shader
#version 330

in vec3 fCol;

void main() {
    gl_FragColor = vec4(fCol, 1.);
}