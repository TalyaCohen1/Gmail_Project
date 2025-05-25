const Label = require('../models/labelModel')

exports.getAllLabels = (req, res) => {
    res.json(Label.getAllLabels())
}

exports.getLabelById = (req, res) => {
    const label = Label.getLabel(parseInt(req.params.id))
    if (!label) {
        return res.status(404).json({ error: 'Label not found' })
    }
    res.json(label)
}

exports.createLabel = (req, res) => {
    const { title, content } = req.body
    if (!title || !content) {
        return res.status(400).json({ error: 'Title and content are required' })
    }
    const newLabel = Label.createLabel(title, content)
    res.status(201).location(`/api/labels/${newLabel.id}`).end()
}

exports.updateLabel = (req, res) => {
    const label = Label.getLabel(parseInt(req.params.id))
    if (!label) {
        return res.status(404).json({ error: 'Label not found' })
    }
    const { title, content } = req.body
    if(!title && !content) {
        return res.status(400).json({ error: 'Title or content is required' })
    }
    const updated_label= Label.updateLabel(label.id, title, content)
    if (!updated_label) {
        return res.status(404).json({ error: 'Label not found' })
    }
    res.json(updated_label)
}

exports.deleteLabel = (req, res) => {
    const label = Label.getLabel(parseInt(req.params.id))
    if (!label) {
        return res.status(404).json({ error: 'Label not found' })
    }
    Label.deleteLabel(label.id)
    res.status(204).json()
}