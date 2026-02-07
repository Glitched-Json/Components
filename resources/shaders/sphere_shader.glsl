//$Vertex Shader
#version 330

layout (location = 0) in vec3 vPos; //$ Position

out vec2 fPos;

uniform mat4 view;
uniform mat4 projection;
uniform mat4 transform;

void main() {
    gl_Position = projection * view * transform * vec4(vPos, 1.);
    fPos = vPos.xy;
}

//$Fragment Shader
#version 330

in vec3 fCol;
in vec2 fPos;

uniform vec3 color; //$ sphereColor

void main() {
    if (distance(fPos, vec2(0)) > 0.5) discard;
    gl_FragColor = vec4(color, 1.);
}