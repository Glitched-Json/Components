//$Vertex Shader
#version 330

layout (location = 0) in vec3 vPos; //$ Position
layout (location = 1) in vec3 vCol; //$ Color

out vec3 fCol;

uniform mat4 view;
uniform mat4 projection;
uniform mat4 transform;

void main() {
    gl_Position = projection * view * transform * vec4(vPos, 1.);
    fCol = vCol;
}

//$Fragment Shader
#version 330

in vec3 fCol;

void main() {
    gl_FragColor = vec4(vec3(1)-fCol, 1.);

}